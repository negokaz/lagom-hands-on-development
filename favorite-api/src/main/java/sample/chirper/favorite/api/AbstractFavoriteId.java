package sample.chirper.favorite.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import org.immutables.value.Value;
@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = FavoriteId.class)
public interface AbstractFavoriteId {

    @Value.Parameter
    String getFavoriteId();
}
