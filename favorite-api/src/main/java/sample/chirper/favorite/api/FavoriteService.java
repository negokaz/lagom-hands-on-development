package sample.chirper.favorite.api;

import static com.lightbend.lagom.javadsl.api.Service.*;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.CircuitBreaker;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import org.pcollections.POrderedSet;


public interface FavoriteService extends Service {

    /**
     * お気に入りを追加する
     */
    ServiceCall<FavoriteId, NotUsed> addFavorite(String userId);

    /**
     * お気に入りを削除する
     * ServiceCall<REQUEST, RESPONSE>
     * @param userId ユーザーのID
     */
    ServiceCall<FavoriteId, NotUsed> deleteFavorite(String userId);

    /**
     * お気に入りのリストを取得する
     */
    ServiceCall<NotUsed, POrderedSet<String>> getFavorites(String userId);

    /**
     * お気に入りに登録されている数を取得する
     */
    ServiceCall<NotUsed, Integer> getFavorCount(String favoriteId);

    @Override
    default Descriptor descriptor() {
        // @formatter:off
        return named("favoriteservice").withCalls(
                pathCall("/api/favorites/:userId/add", this::addFavorite),
                pathCall("/api/favorites/:userId/delete", this::deleteFavorite),
                pathCall("/api/favorites/:userId/list", this::getFavorites),
                pathCall("/api/favors/:favoriteId/count", this::getFavorCount)
        ).withAutoAcl(true);
        // @formatter:on
    }
}
