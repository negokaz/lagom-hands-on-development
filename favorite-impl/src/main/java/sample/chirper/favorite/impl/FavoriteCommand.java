package sample.chirper.favorite.impl;

import akka.Done;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.lightbend.lagom.javadsl.immutable.ImmutableStyle;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;
import org.immutables.value.Value;
import org.pcollections.OrderedPSet;
import org.pcollections.POrderedSet;

/**
 * {@link FavoriteEntity} で処理するコマンド
 */
public interface FavoriteCommand extends Jsonable {

    /**
     * お気に入りを追加するコマンド
     */
    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize(as = AddFavorite.class)
    interface AbstractAddFavorite extends FavoriteCommand, PersistentEntity.ReplyType<Done> {

        @Value.Parameter
        String getUserId();

        @Value.Parameter
        String getFavoriteChirpId();
    }

    /**
     * お気に入りを削除するコマンド
     */
    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize(as = DeleteFavorite.class)
    interface AbstractDeleteFavorite extends FavoriteCommand, PersistentEntity.ReplyType<Done> {

        @Value.Parameter
        String getUserId();

        @Value.Parameter
        String getFavoriteChirpId();
    }

    /**
     * お気に入りを取得するコマンド ({@link GetFavoritesReply} で応答を返す)
     */
    @Value.Immutable(singleton = true)
    @ImmutableStyle
    @JsonDeserialize(as = GetFavorites.class)
    interface AbstractGetFavorites extends FavoriteCommand, PersistentEntity.ReplyType<GetFavoritesReply> {
    }


    /**
     *  {@link GetFavorites} に対する応答
     */
    @Value.Immutable
    @ImmutableStyle
    @JsonDeserialize(as = GetFavoritesReply.class)
    interface AbstractGetFavoritesReply extends Jsonable {

        @Value.Default
        default POrderedSet<String> getFavoriteIds() {
            return OrderedPSet.empty();
        }
    }

}
