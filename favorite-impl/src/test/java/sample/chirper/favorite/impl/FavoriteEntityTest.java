package sample.chirper.favorite.impl;

import akka.Done;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class FavoriteEntityTest {

    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create("favorite-entity");
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testAddFavorite() {
        PersistentEntityTestDriver<FavoriteCommand, FavoriteEvent, FavoriteState> driver =
            new PersistentEntityTestDriver<>(system, new FavoriteEntity(), "user-1");

        PersistentEntityTestDriver.Outcome<FavoriteEvent, FavoriteState> outcome =
            driver.run(AddFavorite.of("user-1", "test-chirp-id"));

        assertEquals(Done.getInstance(), outcome.getReplies().get(0));
        Assert.assertEquals("user-1", ((FavoriteAdded) outcome.events().get(0)).getUserId());
        Assert.assertEquals("test-chirp-id", ((FavoriteAdded) outcome.events().get(0)).getChirpId());
        assertEquals(Collections.emptyList(), driver.getAllIssues());
    }

    @Test
    public void testDeleteFavorite() {
        PersistentEntityTestDriver<FavoriteCommand, FavoriteEvent, FavoriteState> driver =
                new PersistentEntityTestDriver<>(system, new FavoriteEntity(), "user-1");

        PersistentEntityTestDriver.Outcome<FavoriteEvent, FavoriteState> outcome =
                driver.run(DeleteFavorite.of("user-1", "test-chirp-id"));

        assertEquals(Done.getInstance(), outcome.getReplies().get(0));
        Assert.assertEquals("user-1", ((FavoriteDeleted) outcome.events().get(0)).getUserId());
        Assert.assertEquals("test-chirp-id", ((FavoriteDeleted) outcome.events().get(0)).getChirpId());
        assertEquals(Collections.emptyList(), driver.getAllIssues());
    }

    @Test
    public void testGetFavoritesWhenNotHaveAnyFavorites() {
        PersistentEntityTestDriver<FavoriteCommand, FavoriteEvent, FavoriteState> driver =
                new PersistentEntityTestDriver<>(system, new FavoriteEntity(), "user-1");

        PersistentEntityTestDriver.Outcome<FavoriteEvent, FavoriteState> outcome =
                driver.run(GetFavorites.of());

        Assert.assertEquals(GetFavoritesReply.builder().build(), outcome.getReplies().get(0));
        assertEquals(Collections.emptyList(), outcome.events());
        assertEquals(Collections.emptyList(), driver.getAllIssues());
    }
}
