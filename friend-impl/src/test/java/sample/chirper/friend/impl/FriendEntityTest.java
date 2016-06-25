/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package sample.chirper.friend.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.pcollections.PCollectionsModule;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pcollections.TreePVector;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver.Outcome;

import akka.Done;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import sample.chirper.friend.api.AbstractUser;
import sample.chirper.friend.api.User;


public class FriendEntityTest {

  static ActorSystem system;

  @BeforeClass
  public static void setup() {
    system = ActorSystem.create("FriendEntityTest");
  }

  @AfterClass
  public static void teardown() {
    JavaTestKit.shutdownActorSystem(system);
    system = null;
  }

  @Test
  public void testCreateUser() {
    PersistentEntityTestDriver<FriendCommand, FriendEvent, FriendState> driver = new PersistentEntityTestDriver<>(
        system, new FriendEntity(), "user-1");

    Outcome<FriendEvent, FriendState> outcome = driver.run(
         CreateUser.of(User.of("alice", "Alice")));
    assertEquals(Done.getInstance(), outcome.getReplies().get(0));
    assertEquals("alice", ((UserCreated) outcome.events().get(0)).getUserId());
    assertEquals("Alice", ((UserCreated) outcome.events().get(0)).getName());
    assertEquals(Collections.emptyList(), driver.getAllIssues());
  }

  @Test
  public void testRejectDuplicateCreate() {
    PersistentEntityTestDriver<FriendCommand, FriendEvent, FriendState> driver = new PersistentEntityTestDriver<>(
        system, new FriendEntity(), "user-1");
    driver.run(CreateUser.of(User.of("alice", "Alice")));

    Outcome<FriendEvent, FriendState> outcome = driver.run(
        CreateUser.of(User.of("alice", "Alice")));
    assertEquals(PersistentEntity.InvalidCommandException.class, outcome.getReplies().get(0).getClass());
    assertEquals(Collections.emptyList(), outcome.events());
    assertEquals(Collections.emptyList(), driver.getAllIssues());
  }

  @Test
  public void testCreateUserWithInitialFriends() {
    PersistentEntityTestDriver<FriendCommand, FriendEvent, FriendState> driver = new PersistentEntityTestDriver<>(
        system, new FriendEntity(), "user-1");

    TreePVector<String> friends = TreePVector.<String>empty().plus("bob").plus("peter");
    Outcome<FriendEvent, FriendState> outcome = driver.run(
        CreateUser.of(AbstractUser.of("alice", "Alice", Optional.of(friends))));
    assertEquals(Done.getInstance(), outcome.getReplies().get(0));
    assertEquals("alice", ((UserCreated) outcome.events().get(0)).getUserId());
    assertEquals("bob", ((FriendAdded) outcome.events().get(1)).getFriendId());
    assertEquals("peter", ((FriendAdded) outcome.events().get(2)).getFriendId());
    assertEquals(Collections.emptyList(), driver.getAllIssues());
  }

  @Test
  public void testAddFriend() {
    PersistentEntityTestDriver<FriendCommand, FriendEvent, FriendState> driver = new PersistentEntityTestDriver<>(
        system, new FriendEntity(), "user-1");
    driver.run(CreateUser.of(User.of("alice", "Alice")));

    Outcome<FriendEvent, FriendState> outcome = driver.run(AddFriend.of("bob"), AddFriend.of("peter"));
    assertEquals(Done.getInstance(), outcome.getReplies().get(0));
    assertEquals("bob", ((FriendAdded) outcome.events().get(0)).getFriendId());
    assertEquals("peter", ((FriendAdded) outcome.events().get(1)).getFriendId());
    assertEquals(Collections.emptyList(), driver.getAllIssues());
  }

  @Test
  public void testAddDuplicateFriend() {
    PersistentEntityTestDriver<FriendCommand, FriendEvent, FriendState> driver = new PersistentEntityTestDriver<>(
        system, new FriendEntity(), "user-1");
    driver.run(CreateUser.of(User.of("alice", "Alice")));
    driver.run(AddFriend.of("bob"), AddFriend.of("peter"));

    Outcome<FriendEvent, FriendState> outcome = driver.run(AddFriend.of("bob"));
    assertEquals(Done.getInstance(), outcome.getReplies().get(0));
    assertEquals(Collections.emptyList(), outcome.events());
    assertEquals(Collections.emptyList(), driver.getAllIssues());
  }

  @Test
  public void testGetUser() {
    PersistentEntityTestDriver<FriendCommand, FriendEvent, FriendState> driver = new PersistentEntityTestDriver<>(
        system, new FriendEntity(), "user-1");
    User alice = User.of("alice", "Alice");
    driver.run(CreateUser.of(alice));

    Outcome<FriendEvent, FriendState> outcome = driver.run(GetUser.of());
    assertEquals(GetUserReply.of(Optional.of(alice)), outcome.getReplies().get(0));
    assertEquals(Collections.emptyList(), outcome.events());
    assertEquals(Collections.emptyList(), driver.getAllIssues());
  }

}
