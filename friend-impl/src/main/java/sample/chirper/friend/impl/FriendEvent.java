/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package sample.chirper.friend.impl;

import java.time.Instant;
import java.util.Optional;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;

public interface FriendEvent extends Jsonable, AggregateEvent<FriendEvent> {

  @Override
  default public AggregateEventTag<FriendEvent> aggregateTag() {
    return FriendEventTag.INSTANCE;
  }

  @Value.Immutable
  @ImmutableStyle
  @JsonDeserialize(as = UserCreated.class)
  interface AbstractUserCreated extends FriendEvent {

    @Value.Parameter
    String getUserId();

    @Value.Parameter
    String getName();

    @Value.Default
    default Instant getTimestamp() {
      return Instant.now();
    }

    static UserCreated of(String userId, String name, Optional<Instant> timestamp) {
      UserCreated.Builder builder = UserCreated.builder()
              .userId(userId)
              .name(name);
      timestamp.ifPresent(t -> builder.timestamp(t));
      return builder.build();
    }

  }


  @Value.Immutable
  @ImmutableStyle
  @JsonDeserialize(as = FriendAdded.class)
  interface AbstractFriendAdded extends FriendEvent {

    @Value.Parameter
    String getUserId();

    @Value.Parameter
    String getFriendId();

    @Value.Default
    default Instant getTimestamp() {
      return Instant.now();
    }

    static FriendAdded of(String userId, String friendId, Optional<Instant> timestamp) {
      FriendAdded.Builder builder = FriendAdded.builder()
              .userId(userId)
              .friendId(friendId);
      timestamp.ifPresent(t -> builder.timestamp(t));
      return builder.build();
    }
  }
}
