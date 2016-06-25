package sample.chirper.favorite.impl;/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pcollections.POrderedSet;
import sample.chirper.favorite.api.FavoriteId;
import sample.chirper.favorite.api.FavoriteService;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

public class FavoriteServiceTest {

  private static TestServer server;

  @BeforeClass
  public static void setUp() {
    server = startServer(defaultSetup());
  }

  @AfterClass
  public static void tearDown() {
    server.stop();
    server = null;
  }

  @Test
  public void shouldBeAbleToAddAndDeleteFavorites() throws Exception {
      FavoriteService favoriteService = server.client(FavoriteService.class);
      FavoriteId fav1 = FavoriteId.of("favorite-A-1");
      favoriteService.addFavorite("user-A").invoke(fav1).toCompletableFuture().get(10, SECONDS);
      FavoriteId fav2 = FavoriteId.of("favorite-A-2");
      favoriteService.addFavorite("user-A").invoke(fav2).toCompletableFuture().get(3, SECONDS);
      FavoriteId fav3 = FavoriteId.of("favorite-B-1");
      favoriteService.addFavorite("user-B").invoke(fav3).toCompletableFuture().get(3, SECONDS);
      favoriteService.addFavorite("user-C").invoke(fav3).toCompletableFuture().get(3, SECONDS);

      favoriteService.deleteFavorite("user-A").invoke(fav1).toCompletableFuture().get(3, SECONDS);

      POrderedSet<String> userAFavorites =
              favoriteService.getFavorites("user-A").invoke().toCompletableFuture().get(3, SECONDS);

      assertEquals(userAFavorites.size(), 1);
      Assert.assertEquals(fav2.getFavoriteId(), userAFavorites.get(0));

      POrderedSet<String> userBFavorites =
              favoriteService.getFavorites("user-B").invoke().toCompletableFuture().get(3, SECONDS);

      assertEquals(userBFavorites.size(), 1);
      Assert.assertEquals(fav3.getFavoriteId(), userBFavorites.get(0));

      int favorCount =
              favoriteService.getFavorCount("favorite-B-1").invoke().toCompletableFuture().get(3, SECONDS);

      assertEquals(favorCount, 2);
  }

}
