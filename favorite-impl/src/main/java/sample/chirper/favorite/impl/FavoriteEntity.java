package sample.chirper.favorite.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import java.util.Optional;

/**
 * お気に入りに関する属性と振る舞いを持つエンティティ (write-side)
 */
public class FavoriteEntity extends PersistentEntity<FavoriteCommand, FavoriteEvent, FavoriteState> {

    @Override
    public Behavior initialBehavior(Optional<FavoriteState> snapshotState) {
        BehaviorBuilder b = newBehaviorBuilder(snapshotState.orElse(FavoriteState.builder().build()));

        return b.build();
    }
}
