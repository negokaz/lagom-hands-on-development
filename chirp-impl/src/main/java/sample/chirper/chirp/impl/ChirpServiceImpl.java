/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package sample.chirper.chirp.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.pcollections.POrderedSet;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;

import com.datastax.driver.core.Row;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import com.lightbend.lagom.javadsl.pubsub.PubSubRef;
import com.lightbend.lagom.javadsl.pubsub.PubSubRegistry;
import com.lightbend.lagom.javadsl.pubsub.TopicId;

import akka.Done;
import akka.NotUsed;
import akka.stream.javadsl.Source;
import play.Logger;
import play.Logger.ALogger;
import sample.chirper.chirp.api.*;
import sample.chirper.favorite.api.FavoriteService;

public class ChirpServiceImpl implements ChirpService {

  private static final int MAX_TOPICS = 1024;
  private final PubSubRegistry topics;
  private final CassandraSession db;
  private final FavoriteService favoriteService;
  private final ALogger log = Logger.of(getClass());

  @Inject
  public ChirpServiceImpl(PubSubRegistry topics, CassandraSession db, FavoriteService favoriteService) {
    this.topics = topics;
    this.db = db;
    createTable();
    this.favoriteService = favoriteService;
  }

  private void createTable() {
    // @formatter:off
    CompletionStage<Done> result = db.executeCreateTable(
        "CREATE TABLE IF NOT EXISTS chirp ("
        + "userId text, timestamp bigint, uuid text, message text, "
        + "PRIMARY KEY (userId, timestamp, uuid))");
    // @formatter:on
    result.whenComplete((ok, err) -> {
      if (err != null) {
        log.error("Failed to create chirp table, due to: " + err.getMessage(), err);
      }
    });
  }

  @Override
  public ServiceCall<Chirp, NotUsed> addChirp(String userId) {
    return chirp -> {
      if (!userId.equals(chirp.getUserId()))
        throw new IllegalArgumentException("UserId " + userId + " did not match userId in " + chirp);
      PubSubRef<Chirp> topic = topics.refFor(TopicId.of(Chirp.class, topicQualifier(userId)));
      topic.publish(chirp);
      CompletionStage<NotUsed> result =
        db.executeWrite("INSERT INTO chirp (userId, uuid, timestamp, message) VALUES (?, ?, ?, ?)",
          chirp.getUserId(), chirp.getUuid(), chirp.getTimestamp().toEpochMilli(),
          chirp.getMessage()).thenApply(done -> NotUsed.getInstance());
      return result;
    };
  }

  private String topicQualifier(String userId) {
    return String.valueOf(Math.abs(userId.hashCode()) % MAX_TOPICS);
  }

  @Override
  public ServiceCall<LiveChirpsRequest, Source<Chirp, ?>> getLiveChirps(String userId) {
    return req -> {

      CompletionStage<Source<Chirp, ?>> recentChirpSource =
        recentChirps(req.getUserIds()).thenApply(recentChirps -> {
          List<Source<Chirp, ?>> sources = new ArrayList<>();
          for (String uid : req.getUserIds()) {
            PubSubRef<Chirp> topic = topics.refFor(TopicId.of(Chirp.class, topicQualifier(uid)));
            sources.add(topic.subscriber());
          }
          HashSet<String> users = new HashSet<>(req.getUserIds());
          Source<Chirp, ?> publishedChirps = Source.from(sources).flatMapMerge(sources.size(), s -> s)
                  .filter(c -> users.contains(c.getUserId()));

          // We currently ignore the fact that it is possible to get duplicate chirps
          // from the recent and the topic. That can be solved with a de-duplication stage.
          return Source.from(recentChirps).concat(publishedChirps);
        });

      // お気に入りの一覧
      CompletionStage<POrderedSet<String>> favorites = favoriteService.getFavorites(userId).invoke();

      return recentChirpSource.thenApply(source -> { // source: つぶやきのストリーム

        return source.mapAsync(2, chirp -> {
          // STEP2 - お気に入りされた数を取得
          CompletionStage<Integer> favorCount =
                  favoriteService.getFavorCount(chirp.getUuid()).invoke();

          CompletionStage<Chirp> chirpAppliedFavorite =
            favorites.thenCompose(favs ->
            favorCount.thenApply(count ->
              // お気に入りされている場合は isFavorite を true にする
              // お気に入りされた数を favorCount に設定
              favs.contains(chirp.getUuid())
                      ? chirp.withIsFavorite(true).withFavorCount(count)
                      : chirp.withFavorCount(count)
            ));
          return chirpAppliedFavorite;
        });
      });
    };
  }

  @Override
  public ServiceCall<HistoricalChirpsRequest, Source<Chirp, ?>> getHistoricalChirps() {
    return req -> {
      List<Source<Chirp, ?>> sources = new ArrayList<>();
      for (String uid : req.getUserIds()) {
          Source<Chirp, NotUsed> select = db
            .select("SELECT * FROM chirp WHERE userId = ? AND timestamp >= ? ORDER BY timestamp ASC", uid,
                req.getFromTime().toEpochMilli())
            .map(this::mapChirp);
        sources.add(select);
      }
        // Chirps from one user are ordered by timestamp, but chirps from different
        // users are not ordered. That can be improved by implementing a smarter
        // merge that takes the timestamps into account.
      Source<Chirp, ?> result = Source.from(sources).flatMapMerge(sources.size(), s -> s);
      return CompletableFuture.completedFuture(result);
    };
  }

  private Chirp mapChirp(Row row) {
    return AbstractChirp.of(row.getString("userId"), row.getString("message"),
        Optional.of(Instant.ofEpochMilli(row.getLong("timestamp"))), Optional.of(row.getString("uuid")), Optional.empty(), Optional.empty());
  }

  private CompletionStage<PSequence<Chirp>> recentChirps(PSequence<String> userIds) {
    int limit = 10;
    PSequence<CompletionStage<PSequence<Chirp>>> results = TreePVector.empty();
    for (String userId : userIds) {
      CompletionStage<PSequence<Chirp>> result = db
          .selectAll("SELECT * FROM chirp WHERE userId = ? ORDER BY timestamp DESC LIMIT ?", userId, limit)
          .thenApply(rows -> {
            List<Chirp> chirps = rows.stream().map(this::mapChirp).collect(Collectors.toList());
            return TreePVector.from(chirps);
          });
      results = results.plus(result);
    }

    CompletionStage<PSequence<Chirp>> combined = null;
    for (CompletionStage<PSequence<Chirp>> chirpsFromOneUser : results) {
      if (combined == null) {
        combined = chirpsFromOneUser;
      } else {
        combined = combined.thenCombine(chirpsFromOneUser, (a, b) -> a.plusAll(b));
      }
    }

    CompletionStage<PSequence<Chirp>> sortedLimited = combined.thenApply(all -> {
      List<Chirp> allSorted = new ArrayList<>(all);
      // reverse order
      Collections.sort(allSorted, (a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
      List<Chirp> limited = allSorted.stream().limit(limit).collect(Collectors.toList());
      List<Chirp> reversed = new ArrayList<>(limited);
      Collections.reverse(reversed);
      return TreePVector.from(reversed);
    });

    return sortedLimited;
  }

}
