package sample.chirper.favorite.impl;

import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;


public class FavoriteEventTag {

    public static final AggregateEventTag<FavoriteEvent> INSTANCE =
            AggregateEventTag.of(FavoriteEvent.class);

}
