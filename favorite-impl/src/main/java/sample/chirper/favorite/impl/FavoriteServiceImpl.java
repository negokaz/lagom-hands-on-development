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

    private PersistentEntityRegistry persistentEntities;

    private final CassandraSession db;

    @Inject
    public FavoriteServiceImpl(PersistentEntityRegistry persistentEntities,
                               CassandraReadSide readSide,
                               CassandraSession db) {
        this.persistentEntities = persistentEntities;
        this.persistentEntities.register(FavoriteEntity.class);
        this.db = db;

        readSide.register(FavoriteEventProcessor.class);
    }

    private PersistentEntityRef<FavoriteCommand> favoriteEntityRef(String userId) {
        return this.persistentEntities.refFor(FavoriteEntity.class, userId);
    }

    @Override
    public ServiceCall<FavoriteId, NotUsed> addFavorite(String userId) {
        return request -> {
            CompletionStage<Done> adding =
                    favoriteEntityRef(userId).ask(AddFavorite.of(userId, request.getChirpId()));
            return adding.thenApply(ack -> NotUsed.getInstance());
        };
    }

    @Override
    public ServiceCall<FavoriteId, NotUsed> deleteFavorite(String userId) {
        return request -> {
            // STEP3 - DeleteFavorite(コマンド)をFavoriteEntityに送る
            CompletionStage<Done> deleting =
                    favoriteEntityRef(userId).ask(DeleteFavorite.of(userId, request.getChirpId()));
            return deleting.thenApply(ack -> NotUsed.getInstance());
        };
    }

    @Override
    public ServiceCall<NotUsed, POrderedSet<String>> getFavorites(String userId) {
        return notUsed -> {
            CompletionStage<GetFavoritesReply> favorites =
                    favoriteEntityRef(userId).ask(GetFavorites.of());
            return favorites.thenApply(rep -> rep.getChirpIds());
        };
    }

    @Override
    public ServiceCall<NotUsed, Integer> getFavorCount(String favoriteId) {
        return notUsed -> {
            return db.selectOne("SELECT COUNT(*) AS favor_count FROM favor WHERE favoriteId = ?", favoriteId)
                    .thenApply(optionalRow ->
                        optionalRow.map(r -> r.getLong("favor_count")).orElse(0L).intValue()
                    );

        };
    }
}
