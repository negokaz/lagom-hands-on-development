/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package sample.chirper.activity.api;

import java.time.Instant;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import org.immutables.value.Value;


@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = HistoricalActivityStreamReq.class)
public interface AbstractHistoricalActivityStreamReq {

  @Value.Parameter
  Instant getFromTime();

}
