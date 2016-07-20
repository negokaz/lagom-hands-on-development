/*
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
package sample.chirper.friend.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import akka.Done;
import sample.chirper.friend.api.User;

public class FriendEntity extends PersistentEntity<FriendCommand, FriendEvent, FriendState> {

  @Override
  public Behavior initialBehavior(Optional<FriendState> snapshotState) {

    BehaviorBuilder b = newBehaviorBuilder(snapshotState.orElse(
      FriendState.of(Optional.empty())));

    b.setCommandHandler(CreateUser.class, (cmd, ctx) -> {
      if (state().getUser().isPresent()) {
        ctx.invalidCommand("User " + entityId() + " is already created");
        return ctx.done();
      } else {
        User user = cmd.getUser();
        List<FriendEvent> events = new ArrayList<FriendEvent>();
        events.add(UserCreated.of(user.getUserId(), user.getName()));
        for (String friendId : user.getFriends()) {
          events.add(FriendAdded.of(user.getUserId(), friendId));
        }
        return ctx.thenPersistAll(events, () -> ctx.reply(Done.getInstance()));
      }
    });

    b.setEventHandler(UserCreated.class,
        evt -> FriendState.of(Optional.of(User.of(evt.getUserId(), evt.getName()))));

    b.setCommandHandler(AddFriend.class, (cmd, ctx) -> {
      if (!state().getUser().isPresent()) {
        ctx.invalidCommand("User " + entityId() + " is not  created");
        return ctx.done();
      } else if (state().getUser().get().getFriends().contains(cmd.getFriendUserId())) {
        ctx.reply(Done.getInstance());
        return ctx.done();
      } else {
        return ctx.thenPersist(FriendAdded.of(getUserId(), cmd.getFriendUserId()), evt ->
          ctx.reply(Done.getInstance()));
      }
    });

    b.setEventHandler(FriendAdded.class, evt -> state().addFriend(evt.getFriendId()));

    b.setReadOnlyCommandHandler(GetUser.class, (cmd, ctx) -> {
      ctx.reply(GetUserReply.of(state().getUser()));
    });

    return b.build();
  }

  private String getUserId() {
    return state().getUser().get().getUserId();
  }
}
