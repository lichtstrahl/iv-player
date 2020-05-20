package root.iv.ivplayer.game.tictac;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import root.iv.ivplayer.game.controller.SensorController;
import root.iv.ivplayer.game.object.DrawableObject;
import root.iv.ivplayer.game.object.Group;
import root.iv.ivplayer.game.object.ObjectGenerator;
import root.iv.ivplayer.game.object.StaticObject2;
import root.iv.ivplayer.game.scene.Scene;

public class TicTacToeScene implements Scene {

    // Генераторы для создания объектов
    private ObjectGenerator backgroundGenerator;
    private TicTacTextures textures;

    // Объекты для отрисовки
    private List<DrawableObject> drawableObjects;
    private Group grid;

    // Элменты управления
    private SensorController controller;

    public TicTacToeScene(TicTacTextures textures, int squareSize, int startMargin, int topMargin) {
        this.textures = textures;


        // Генератор для фона
        backgroundGenerator = new ObjectGenerator();
        backgroundGenerator.setDrawable(textures.getSquare());
        backgroundGenerator.setFixSize(squareSize, squareSize);


        // Формирование сетки
        grid = Group.empty();
        for (int i = 0; i < 9; i++) {
            StaticObject2 square = backgroundGenerator.buildStatic(startMargin + (i % 3)*squareSize, topMargin + (i /3) * squareSize);
            Block block = Block.of(square, textures.getCross(), textures.getCircle());
            grid.add(block);
        }

        // Прочие отрисовываемые объекты
        this.drawableObjects = new ArrayList<>();
        // Контроллер для управления касаниями
        this.controller = new TicTacController();
    }

    public List<Block> getAllBlocks() {
        return this.grid.getObjects()
                .stream()
                .map(obj -> (Block)obj)
                .collect(Collectors.toList());
    }

    @Override
    public void render(Canvas canvas) {
        // Заливка фона
        canvas.drawColor(textures.getBackground());
        // Отрисовка поля
        grid.getObjects().forEach(obj -> obj.render(canvas));

        drawableObjects.forEach(obj -> obj.render(canvas));
    }

    @Override
    public SensorController getMainController() {
        return controller;
    }

    @Override
    public void addDrawableObject(DrawableObject object2) {

    }

    @Override
    public void moveOnObject(int index, float dx, float dy) {

    }
}
