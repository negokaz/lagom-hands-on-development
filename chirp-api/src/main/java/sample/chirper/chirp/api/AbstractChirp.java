/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package sample.chirper.chirp.api;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;

@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = Chirp.class)
public interface AbstractChirp extends Jsonable {

  @Value.Parameter
  String getUserId();

  @Value.Parameter
  String getMessage();

  @Value.Default
  default Instant getTimestamp() {
    return Instant.now();
  }

  @Value.Default
  default String getUuid() {
    return UUID.randomUUID().toString();
  }

  @Value.Default
  default Boolean isFavorite() {
    return false;
  }

  @Value.Default
  default Integer getFavorCount() {
    return 0;
  }

  static Chirp of(String userId,
                  String message,
                  Optional<Instant> timestamp,
                  Optional<String> uuid,
                  Optional<Boolean> isFavorite,
                  Optional<Integer> favorCount) {

    Chirp.Builder builder = Chirp.builder()
            .userId(userId)
            .message(message);

    timestamp.ifPresent(t -> builder.timestamp(t));
    uuid.ifPresent(u -> builder.uuid(u));
    isFavorite.ifPresent(f -> builder.isFavorite(f));
    favorCount.ifPresent(c -> builder.favorCount(c));

    return builder.build();
  }


}