/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package sample.chirper.friend.impl;

import java.util.Optional;

import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import org.immutables.value.Value;
import org.pcollections.PSequence;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.serialization.Jsonable;

import sample.chirper.friend.api.AbstractUser;
import sample.chirper.friend.api.User;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = FriendState.class)
interface AbstractFriendState extends Jsonable {

  @Value.Parameter
  Optional<User> getUser();

  default FriendState addFriend(String friendUserId) {
    Optional<User> user = getUser();
    if (!user.isPresent())
      throw new IllegalStateException("friend can't be added before user is created");
    PSequence<String> newFriends = user.get().getFriends().plus(friendUserId);
    return FriendState.of(Optional.of(AbstractUser.of(user.get().getUserId(), user.get().getName(), Optional.of(newFriends))));
  }
}