package root.iv.ivplayer.game.scene;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

import root.iv.ivplayer.game.TicTacTextures;
import root.iv.ivplayer.game.controller.Controller;
import root.iv.ivplayer.game.object.DrawableObject;
import root.iv.ivplayer.game.object.Group;
import root.iv.ivplayer.game.object.ObjectGenerator;
import root.iv.ivplayer.game.object.Player;
import root.iv.ivplayer.game.object.simple.Object2;
import root.iv.ivplayer.network.ws.pubnub.dto.PlayerPositionDTO;

public class TicTacToeScene implements Scene {
    private static final int SQUARE_SIZE = 150;

    // Генераторы для создания объектов
    private ObjectGenerator backgroundGenerator;

    private TicTacTextures textures;

    // Объекты для отрисовки
    private List<DrawableObject> drawableObjects;
    private Group background;

    public TicTacToeScene(TicTacTextures textures) {
        this.textures = textures;
        // Подготовка для фона
        backgroundGenerator = new ObjectGenerator();
        backgroundGenerator.setDrawable(textures.getSquare());
        backgroundGenerator.setFixSize(SQUARE_SIZE, SQUARE_SIZE);

        int startMargin = 100;
        int topMargin = 100;
        background = Group.empty();
        for (int i = 0; i < 9; i++) {
            DrawableObject square = backgroundGenerator.buildStatic(startMargin + (i % 3)*SQUARE_SIZE, topMargin + (i /3) * SQUARE_SIZE);
            background.add(square);
        }

        this.drawableObjects = new ArrayList<>();
    }

    @Override
    public void render(Canvas canvas) {
        // Заливка фона
        canvas.drawColor(textures.getBackground());
        // Отрисовка поля
        background.getObjects().forEach(obj -> obj.render(canvas));

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
