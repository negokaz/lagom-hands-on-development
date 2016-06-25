/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package sample.chirper.friend.api;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonTypeId;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import org.immutables.value.Value;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;


@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = User.class)
public interface AbstractUser {

  @Value.Parameter
  String getUserId();

  @Value.Parameter
  String getName();

  @Value.Default
  default PSequence<String> getFriends() {
    return TreePVector.empty();
  }


  static User of(String userId, String name, Optional<PSequence<String>> friends) {
    User.Builder builder =  User.builder()
            .userId(userId)
            .name(name);

    friends.ifPresent(f -> builder.friends(f));

    return builder.build();
  }

}