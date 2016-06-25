/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package sample.chirper.chirp.api;

import java.time.Instant;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import org.immutables.value.Value;
import org.pcollections.PSequence;


@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = HistoricalChirpsRequest.class)
public interface AbstractHistoricalChirpsRequest {

  @Value.Parameter
  Instant getFromTime();

  @Value.Parameter
  PSequence<String> getUserIds();

}