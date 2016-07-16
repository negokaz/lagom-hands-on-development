package sample.chirper.favorite.impl;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;

import java.util.Optional;

/**
 * お気に入りに関する属性と振る舞いを持つエンティティ (write-side)
 *
 * 参照: http://www.lagomframework.com/documentation/1.0.x/java/PersistentEntity.html
 */
public class FavoriteEntity extends PersistentEntity<FavoriteCommand, FavoriteEvent, FavoriteState> {

    /**
     * Entity の"振る舞い"を定義するメソッド。
     *
     * @param snapshotState {@link FavoriteState} のスナップショット
     *                      Entity の生成直後は empty。
     *                      Entity の状態を復元する時間を短くするために、
     *                      一定数のイベントが来たタイミングでそのときの状態が永続化される。
     *                      http://www.lagomframework.com/documentation/1.0.x/java/PersistentEntity.html#snapshots
     */
    @Override
    public Behavior initialBehavior(Optional<FavoriteState> snapshotState) {
        // 振る舞いを構築するビルダー
        BehaviorBuilder b = newBehaviorBuilder(snapshotState.orElse(FavoriteState.builder().build()));

        /*
         * AddFavorite(コマンド)が送られてくる
         *  → FavoriteAdded(イベント)を作成
         *  → イベントを永続化
         *  → Done を送り返す
         */
        b.setCommandHandler(AddFavorite.class,
            (request, ctx) -> {
                FavoriteAdded event = FavoriteAdded.of(request.getUserId(), request.getChirpId());
                return ctx.thenPersist(event, (evt) -> ctx.reply(Done.getInstance()));
            }
        );
        // FavoriteAdded イベントが起きたときは状態に chirpId を追加
        b.setEventHandler(FavoriteAdded.class,
            (evt) -> state().addChirpId(evt.getChirpId())
        );

        /*
         * STEP3 - DeleteFavorite(コマンド)が送られてくる
         *  → FavoriteDeleted(イベント)を作成
         *  → イベントを永続化
         *  → Done を送り返す
         */
        b.setCommandHandler(DeleteFavorite.class,
            (request, ctx) -> {
                FavoriteDeleted event = FavoriteDeleted.of(request.getUserId(), request.getChirpId());
                return ctx.thenPersist(event, (evt) -> ctx.reply(Done.getInstance()));
            }
        );
        // STEP3 - FavoriteDeleted(イベント)が起きたときは状態から chirpId を削除
        b.setEventHandler(FavoriteDeleted.class,
            (evt) -> state().deleteChirpId(evt.getChirpId())
        );

        /*
         * GetFavorites(コマンド)が送られてくる
         *  → GetFavoritesReply(イベント)を作成して送り返す
         */
        b.setReadOnlyCommandHandler(GetFavorites.class,
            (request, ctx) -> {
                GetFavoritesReply favorites = GetFavoritesReply.builder()
                        .chirpIds(state().getChirpIds())
                        .build();
                ctx.reply(favorites);
            }
        );

        return b.build();
    }
}
