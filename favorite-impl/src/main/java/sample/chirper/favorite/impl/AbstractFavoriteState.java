package sample.chirper.favorite.impl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;
import org.pcollections.*;

import javax.validation.constraints.NotNull;

/**
 * {@link FavoriteEntity} の状態
 */
@Value.Immutable
@ImmutableStyle
@JsonDeserialize(as = FavoriteState.class)
public interface AbstractFavoriteState extends Jsonable {

    /**
     * @return favoriteId のセット
     */
    @Value.Default
    default POrderedSet<String> getChirpIds() {
        return OrderedPSet.empty();
    }

    /**
     * 指定された favoriteId を追加する
     * @param favoriteId
     * @return
     */
    default FavoriteState addChirpId(String favoriteId) {
        POrderedSet<String> newFavoriteIds = getChirpIds().plus(favoriteId);
        return FavoriteState.builder().from(this)
                .chirpIds(newFavoriteIds)
                .build();
    }

    /**
     * 指定された favoriteId を削除する
     * @param favoriteId
     * @return
     */
    default FavoriteState deleteChirpId(String favoriteId) {
        POrderedSet<String> newChirpIds = getChirpIds().minus(favoriteId);
        return FavoriteState.builder().from(this)
                .chirpIds(newChirpIds)
                .build();
    }
}
