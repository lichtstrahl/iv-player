package root.iv.ivplayer.game.scene;

import android.graphics.Canvas;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

import root.iv.ivplayer.game.controller.Controller;
import root.iv.ivplayer.game.object.DrawableObject;
import root.iv.ivplayer.game.object.Player;
import root.iv.ivplayer.network.ws.pubnub.dto.PlayerPositionDTO;

public class TestScene implements Scene {
    private Long lastFrameMS = 0L;
    // Список объектов, которые можно отрисовать
    private List<DrawableObject> drawableObjects;
    private List<String> players;

    public TestScene() {
        drawableObjects = new ArrayList<>();
        players = new ArrayList<>();
    }

    @Override
    public void render(Canvas canvas) {
        long startRender = System.currentTimeMillis();

        canvas.drawColor(Color.BLACK);
        drawableObjects.forEach(obj -> obj.render(canvas));

        long finishRender = System.currentTimeMillis();
        lastFrameMS = finishRender;
    }

    @Override
    @Deprecated
    public void joinPlayer(String joinUUID, float x, float y) {

    }

    @Override
    @Deprecated
    public Controller getMainController() {
        return null;
    }

    @Override
    public void addDrawableObject(DrawableObject object2) {
        drawableObjects.add(object2);

        // Если добавляется игрок, то его uuid заносится в общий список
        if (object2 instanceof Player) {
            players.add(((Player) object2).getUuid());
        }
    }

    @Override
    @Deprecated
    public void processPlayerPositionDTO(PlayerPositionDTO position) {
    }

    @Override
    @Deprecated
    public void moveOnObject(int index, float dx, float dy) {

    }
}
