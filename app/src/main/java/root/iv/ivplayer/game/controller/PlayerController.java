package root.iv.ivplayer.game.controller;

import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.function.Consumer;

import root.iv.ivplayer.game.object.Player;
import root.iv.ivplayer.game.object.simple.Object2;
import root.iv.ivplayer.game.object.simple.Point2;
import root.iv.ivplayer.network.ws.pubnub.dto.PlayerPositionDTO;
import timber.log.Timber;

// Контроллер игрока аналогичен контроллеру движений
// Помимо кроме того факта, что управление осуществляется не каким-то актером,
// а игроком с UUID. Поэтому после осуществления перемещений
// Формируется соответствубщая информация о координатах игрока и выполняется обработчик для неё
public class PlayerController extends MoveController {
    @Nullable
    private Player player;
    private Consumer<PlayerPositionDTO> positionHandler;

    public PlayerController(Consumer<PlayerPositionDTO> positionHandler) {
        this.positionHandler = positionHandler;
    }

    @Override
    public void onClick(View v) {
        if (player != null) {
            player.moveOn(0, 10);

            Point2 position = player.getPosition();
            PlayerPositionDTO positionDTO =
                    new PlayerPositionDTO(player.getUuid(), position.x, position.y);
            positionHandler.accept(positionDTO);
        }
    }

    @Override
    public void grabObject(Object2 object) {
        if (object instanceof Player) {
            this.player = (Player) object;
        } else {
            Timber.e("В контроллер движения кладётся неподдерживаемый объект");
        }
    }

    @Override
    public void releaseObject() {
        this.player = null;
    }

    @Override
    public boolean isReleased() {
        return player == null;
    }

    @Override
    public void setClickHandler(androidx.core.util.Consumer<View> handler) {

    }

    @Override
    public void setTouchHandler(androidx.core.util.Consumer<MotionEvent> event) {

    }
}
