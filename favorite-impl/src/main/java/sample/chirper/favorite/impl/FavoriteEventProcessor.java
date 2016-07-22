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
        return session.executeCreateTable(
            "CREATE TABLE IF NOT EXISTS favor ("
                + "favoriteId text, favoredBy text, timestamp bigint, "
                + "PRIMARY KEY (favoriteId, favoredBy))").thenCompose(a ->
            session.executeCreateTable(
                "CREATE TABLE IF NOT EXISTS favor_offset ("
                    + "partition int, offset timeuuid, "
                    + "PRIMARY KEY (partition))"));
    }

    private CompletionStage<Done> prepareWriteFavors(CassandraSession session) {
        return session.prepare("INSERT INTO favor (favoriteId, favoredBy, timestamp) VALUES (?, ?, ?)").thenApply(ps -> {
            setWriteFavors(ps);
            return Done.getInstance();
        });
    }

    private CompletionStage<Done> prepareDeleteFavors(CassandraSession session) {
        return session.prepare("DELETE FROM favor WHERE favoriteId = ? AND favoredBy = ?").thenApply(ps -> {
            setDeleteFavors(ps);
            return Done.getInstance();
        });
    }

    private CompletionStage<Done> prepareWriteOffset(CassandraSession session) {
        return session.prepare("INSERT INTO favor_offset (partition, offset) VALUES (1, ?)").thenApply(ps -> {
            setWriteOffset(ps);
            return Done.getInstance();
        });
    }

    private CompletionStage<Optional<UUID>> selectOffset(CassandraSession session) {
        return session
                .selectOne("SELECT offset FROM favor_offset")
                .thenApply(optionalRow -> optionalRow.map(r -> r.getUUID("offset")));
    }

    @Override
    public EventHandlers defineEventHandlers(EventHandlersBuilder builder) {

        builder.setEventHandler(FavoriteAdded.class,
            (event, offset) -> {
                BoundStatement bindWriteFavors = writeFavors.bind()
                        .setString("favoriteId", event.getChirpId())
                        .setString("favoredBy", event.getUserId())
                        .setLong("timestamp", event.getTimestamp().toEpochMilli());
                BoundStatement bindWriteOffset = writeOffset.bind(offset);
                return completedStatements(Arrays.asList(bindWriteFavors, bindWriteOffset));
            }
        );

        builder.setEventHandler(FavoriteDeleted.class,
            (event, offset) -> {
                BoundStatement bindDeleteFavors = deleteFavors.bind()
                        .setString("favoriteId", event.getChirpId())
                        .setString("favoredBy", event.getUserId());
                BoundStatement bindWriteOffset = writeOffset.bind(offset);
                return completedStatements(Arrays.asList(bindDeleteFavors, bindWriteOffset));
            }
        );

        return builder.build();
    }
}
