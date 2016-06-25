/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package sample.chirper.chirp.impl;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.*;
import static java.util.concurrent.TimeUnit.SECONDS;

import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.testkit.ServiceTest.TestServer;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pcollections.OrderedPSet;
import org.pcollections.POrderedSet;
import org.pcollections.TreePVector;
import sample.chirper.chirp.api.*;

import akka.stream.javadsl.Source;
import akka.stream.testkit.TestSubscriber.Probe;
import akka.stream.testkit.javadsl.TestSink;
import sample.chirper.favorite.api.FavoriteId;
import sample.chirper.favorite.api.FavoriteService;

public class ChirpServiceTest {

  private static TestServer server;

  @BeforeClass
  public static void setUp() {
    server = startServer(defaultSetup().withConfigureBuilder(b ->
      b.overrides(bind(FavoriteService.class).to(FavoriteServiceStub.class))
    ));
  }

  @AfterClass
  public static void tearDown() {
    server.stop();
    server = null;
  }

  @Test
  public void shouldPublishShirpsToSubscribers() throws Exception {
    ChirpService chirpService = server.client(ChirpService.class);
    LiveChirpsRequest request = LiveChirpsRequest.of(TreePVector.<String>empty().plus("usr1").plus("usr2"));
    Source<Chirp, ?> chirps1 = chirpService.getLiveChirps("user1").invoke(request).toCompletableFuture().get(3, SECONDS);
    Probe<Chirp> probe1 = chirps1.runWith(TestSink.probe(server.system()), server.materializer());
    probe1.request(10);
    Source<Chirp, ?> chirps2 = chirpService.getLiveChirps("user1").invoke(request).toCompletableFuture().get(3, SECONDS);
    Probe<Chirp> probe2 = chirps2.runWith(TestSink.probe(server.system()), server.materializer());
    probe2.request(10);

    Chirp chirp1 = Chirp.of("usr1", "hello 1");
    chirpService.addChirp("usr1").invoke(chirp1).toCompletableFuture().get(3, SECONDS);
    probe1.expectNext(chirp1);
    probe2.expectNext(chirp1);

    Chirp chirp2 = Chirp.of("usr1", "hello 2");
    chirpService.addChirp("usr1").invoke(chirp2).toCompletableFuture().get(3, SECONDS);
    probe1.expectNext(chirp2);
    probe2.expectNext(chirp2);

    Chirp chirp3 = Chirp.of("usr2", "hello 3");
    chirpService.addChirp("usr2").invoke(chirp3).toCompletableFuture().get(3, SECONDS);
    probe1.expectNext(chirp3);
    probe2.expectNext(chirp3);

    probe1.cancel();
    probe2.cancel();
  }

  @Test
  public void shouldIncludeSomeOldChirpsInLiveFeed() throws Exception {
    ChirpService chirpService = server.client(ChirpService.class);

    Chirp chirp1 = Chirp.of("usr3", "hi 1");
    chirpService.addChirp("usr3").invoke(chirp1).toCompletableFuture().get(3, SECONDS);

    Chirp chirp2 = Chirp.of("usr4", "hi 2");
    chirpService.addChirp("usr4").invoke(chirp2).toCompletableFuture().get(3, SECONDS);

    LiveChirpsRequest request = LiveChirpsRequest.of(TreePVector.<String>empty().plus("usr3").plus("usr4"));
    Source<Chirp, ?> chirps = chirpService.getLiveChirps("user3").invoke(request).toCompletableFuture().get(3, SECONDS);
    Probe<Chirp> probe = chirps.runWith(TestSink.probe(server.system()), server.materializer());
    probe.request(10);
    probe.expectNextUnordered(chirp1, chirp2);

    Chirp chirp3 = Chirp.of("usr4", "hi 3");
    chirpService.addChirp("usr4").invoke(chirp3).toCompletableFuture().get(3, SECONDS);
    probe.expectNext(chirp3);

    probe.cancel();
  }

  @Test
  public void shouldRetrieveOldChirps() throws Exception {
    ChirpService chirpService = server.client(ChirpService.class);

    Chirp chirp1 = Chirp.of("usr5", "msg 1");
    chirpService.addChirp("usr5").invoke(chirp1).toCompletableFuture().get(3, SECONDS);

    Chirp chirp2 = Chirp.of("usr6", "msg 2");
    chirpService.addChirp("usr6").invoke(chirp2).toCompletableFuture().get(3, SECONDS);

    HistoricalChirpsRequest request = HistoricalChirpsRequest.of(Instant.now().minusSeconds(20),
        TreePVector.<String>empty().plus("usr5").plus("usr6"));
    Source<Chirp, ?> chirps = chirpService.getHistoricalChirps().invoke(request).toCompletableFuture().get(3, SECONDS);
    Probe<Chirp> probe = chirps.runWith(TestSink.probe(server.system()), server.materializer());
    probe.request(10);
    probe.expectNextUnordered(chirp1, chirp2);
    probe.expectComplete();
  }

  public static class FavoriteServiceStub implements FavoriteService {

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
      return request -> {
        return CompletableFuture.completedFuture(OrderedPSet.empty());
      };
    }

    @Override
    public ServiceCall<NotUsed, Integer> getFavorCount(String favoriteId) {
      return request -> {
        return CompletableFuture.completedFuture(Integer.valueOf(0));
      };
    }
  }

}
