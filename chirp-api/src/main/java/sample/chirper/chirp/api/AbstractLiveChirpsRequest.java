/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package sample.chirper.chirp.api;


import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import org.immutables.value.Value;
import org.pcollections.PSequence;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;


@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = LiveChirpsRequest.class)
public interface AbstractLiveChirpsRequest {

  @Value.Parameter
  PSequence<String> getUserIds();

}
