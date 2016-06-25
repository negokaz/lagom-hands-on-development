package sample.chirper.favorite.impl;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import org.pcollections.OrderedPSet;
import org.pcollections.POrderedSet;
import sample.chirper.favorite.api.FavoriteId;
import sample.chirper.favorite.api.FavoriteService;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CompletableFuture;

/**
 * {@link FavoriteService} の実装
 */
public class FavoriteServiceImpl implements FavoriteService {

    public FavoriteServiceImpl() {
    }

    @Override
    public ServiceCall<FavoriteId, NotUsed> addFavorite(String userId) {
        return request -> {
            return CompletableFuture.completedFuture(NotUsed.getInstance());
        };
    }

    @Override
    public ServiceCall<FavoriteId, NotUsed> deleteFavorite(String userId) {
        return request -> {
            return CompletableFuture.completedFuture(NotUsed.getInstance());
        };
    }

    @Override
    public ServiceCall<NotUsed, POrderedSet<String>> getFavorites(String userId) {
        return notUsed -> {
            return CompletableFuture.completedFuture(OrderedPSet.empty());
        };
    }

    @Override
    public ServiceCall<NotUsed, Integer> getFavorCount(String favoriteId) {
        return notUsed -> {
            return CompletableFuture.completedFuture(0);
        };
    }
}
