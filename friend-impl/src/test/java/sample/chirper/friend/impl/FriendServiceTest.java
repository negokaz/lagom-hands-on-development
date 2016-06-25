/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package sample.chirper.friend.impl;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.*;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import sample.chirper.friend.api.FriendId;
import sample.chirper.friend.api.FriendService;
import sample.chirper.friend.api.User;
import scala.concurrent.duration.FiniteDuration;

import akka.NotUsed;

public class FriendServiceTest {

  @Test
  public void shouldBeAbleToCreateUsersAndConnectFriends() throws Exception {
    withServer(defaultSetup(), server -> {
      FriendService friendService = server.client(FriendService.class);
      User usr1 = User.of("usr1", "User 1");
      friendService.createUser().invoke(usr1).toCompletableFuture().get(10, SECONDS);
      User usr2 = User.of("usr2", "User 2");
      friendService.createUser().invoke(usr2).toCompletableFuture().get(3, SECONDS);
      User usr3 = User.of("usr3", "User 3");
      friendService.createUser().invoke(usr3).toCompletableFuture().get(3, SECONDS);

      friendService.addFriend("usr1").invoke(FriendId.of(usr2.getUserId())).toCompletableFuture().get(3, SECONDS);
      friendService.addFriend("usr1").invoke(FriendId.of(usr3.getUserId())).toCompletableFuture().get(3, SECONDS);

      User fetchedUsr1 = friendService.getUser("usr1").invoke().toCompletableFuture().get(3,
          SECONDS);
      assertEquals(usr1.getUserId(), fetchedUsr1.getUserId());
      assertEquals(usr1.getName(), fetchedUsr1.getName());
      assertEquals(TreePVector.empty().plus("usr2").plus("usr3"), fetchedUsr1.getFriends());

      eventually(FiniteDuration.create(10, SECONDS), () -> {
        PSequence<String> followers = friendService.getFollowers("usr2").invoke()
            .toCompletableFuture().get(3, SECONDS);
        assertEquals(TreePVector.empty().plus("usr1"), followers);
      });

    });
  }



}
