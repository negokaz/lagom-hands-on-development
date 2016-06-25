package sample.chirper.favorite.impl;

import akka.Done;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * {@link FavoriteEvent} を処理するイベントプロセッサ (read-side)
 */
public class FavoriteEventProcessor extends CassandraReadSideProcessor<FavoriteEvent> {


    private PreparedStatement writeFavors = null;
    private PreparedStatement deleteFavors = null;
    private PreparedStatement writeOffset = null;

    private void setWriteFavors(PreparedStatement writeFavors) {
        this.writeFavors = writeFavors;
    }

    private void setWriteOffset(PreparedStatement writeOffset) {
        this.writeOffset = writeOffset;
    }

    private void setDeleteFavors(PreparedStatement deleteFavors) {
        this.deleteFavors = deleteFavors;
    }

    @Override
    public AggregateEventTag<FavoriteEvent> aggregateTag() {
        return FavoriteEventTag.INSTANCE;
    }

    @Override
    public CompletionStage<Optional<UUID>> prepare(CassandraSession session) {
        // @formatter:off
        return prepareCreateTables(session).thenCompose(a ->
                prepareWriteFavors(session).thenCompose(b ->
                prepareDeleteFavors(session).thenCompose(c ->
                prepareWriteOffset(session).thenCompose(d ->
                selectOffset(session)))));
        // @formatter:on
    }

    private CompletionStage<Done> prepareCreateTables(CassandraSession session) {
        return CompletableFuture.completedFuture(Done.getInstance());
    }

    private CompletionStage<Done> prepareWriteFavors(CassandraSession session) {
        return CompletableFuture.completedFuture(Done.getInstance());
    }

    private CompletionStage<Done> prepareDeleteFavors(CassandraSession session) {
        return CompletableFuture.completedFuture(Done.getInstance());
    }

    private CompletionStage<Done> prepareWriteOffset(CassandraSession session) {
        return CompletableFuture.completedFuture(Done.getInstance());
    }

    private CompletionStage<Optional<UUID>> selectOffset(CassandraSession session) {
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public EventHandlers defineEventHandlers(EventHandlersBuilder builder) {

        return builder.build();
    }
}
