package root.iv.ivplayer.game.scene;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import java.util.List;

import root.iv.ivplayer.game.controller.Controller;
import root.iv.ivplayer.game.object.DrawableObject;
import root.iv.ivplayer.game.object.Player;
import root.iv.ivplayer.game.object.simple.Object2;
import root.iv.ivplayer.network.ws.pubnub.dto.PlayerPositionDTO;

public class TicTacToeScene implements Scene {

    private List<DrawableObject> drawableObjects;
    private DrawableObject background;

    public TicTacToeScene(Drawable background) {
//        this.background = background;

    }

    @Override
    public void render(Canvas canvas) {
        background.render(canvas);
        drawableObjects.forEach(obj -> obj.render(canvas));
    }

    @Override
    public void joinPlayer(String joinUUID, float x, float y) {

    }

    @Override
    public Controller getMainController() {
        return null;
    }

    @Override
    public void addDrawableObject(DrawableObject object2) {

    }

    @Override
    public Player addPlayer(int x0, int y0, String uuid) {
        return null;
    }

    @Override
    public void processPlayerPositionDTO(PlayerPositionDTO position) {

    }

    @Override
    public void moveOnObject(int index, float dx, float dy) {

    }

    @Override
    public void removePlayer(String uuid) {

    }

    @Override
    public void grabObjectControl(Object2 object) {

    }
}
