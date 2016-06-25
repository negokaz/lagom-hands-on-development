/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package sample.chirper.friend.impl;

import java.util.Optional;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;

import akka.Done;
import org.immutables.value.Value;
import sample.chirper.friend.api.User;

public interface FriendCommand extends Jsonable {

  @Value.Immutable
  @ImmutableStyle
  @JsonDeserialize(as = CreateUser.class)
  interface AbstractCreateUser extends FriendCommand, PersistentEntity.ReplyType<Done> {

    @Value.Parameter
    User getUser();

  }

  @Value.Immutable(singleton = true)
  @ImmutableStyle
  @JsonDeserialize(as = GetUser.class)
  interface AbstractGetUser extends FriendCommand, PersistentEntity.ReplyType<GetUserReply> {
  }

  @Value.Immutable
  @ImmutableStyle
  @JsonDeserialize(as = GetUserReply.class)
  interface AbstractGetUserReply extends Jsonable {

    @Value.Parameter
    Optional<User> getUser();

  }

  @Value.Immutable
  @ImmutableStyle
  @JsonDeserialize(as = AddFriend.class)
  interface AbstractAddFriend extends FriendCommand, PersistentEntity.ReplyType<Done> {

    @Value.Parameter
   String getFriendUserId();

  }

}
