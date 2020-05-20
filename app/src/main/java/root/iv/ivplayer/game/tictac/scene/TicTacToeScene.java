package root.iv.ivplayer.game.tictac.scene;

import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import root.iv.ivplayer.game.controller.SensorController;
import root.iv.ivplayer.game.object.DrawableObject;
import root.iv.ivplayer.game.object.Group;
import root.iv.ivplayer.game.object.ObjectGenerator;
import root.iv.ivplayer.game.object.StaticObject2;
import root.iv.ivplayer.game.scene.Scene;
import root.iv.ivplayer.game.tictac.Block;
import root.iv.ivplayer.game.tictac.TicTacController;
import root.iv.ivplayer.game.tictac.TicTacTextures;

public class TicTacToeScene implements Scene {

    // Генераторы для создания объектов
    private ObjectGenerator squareGenerator;
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
        squareGenerator = new ObjectGenerator();
        squareGenerator.setDrawable(textures.getSquare());
        squareGenerator.setFixSize(squareSize, squareSize);
        squareGenerator.setTintColor(textures.getSquareColor());

        backgroundGenerator = new ObjectGenerator();
        backgroundGenerator.setDrawable(textures.getBackground());

        // Формирование сетки
        grid = gridConstruct(startMargin, topMargin, squareSize);

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

    public Group gridConstruct(int startMargin, int topMargin, int squareSize) {
        Group group = Group.empty();
        for (int i = 0; i < 9; i++) {
            StaticObject2 square = squareGenerator.buildStatic(
                    startMargin + (i % 3)*squareSize,
                    topMargin + (i /3) * squareSize);
            Block block = Block.of(square, textures.getCross(), textures.getCircle(), textures.getCrossColor(), textures.getCircleColor());
            group.add(block);
        }

        return group;
    }

    @Override
    public void render(Canvas canvas) {
        // Заливка фона
        if (backgroundGenerator.hasTexture()) {
            backgroundGenerator.setFixSize(canvas.getWidth(), canvas.getHeight());
            backgroundGenerator.buildStatic(0, 0).render(canvas);
        } else {
            canvas.drawColor(textures.getBackgroundColor());
        }

        // Отрисовка поля
        grid.render(canvas);

        drawableObjects.forEach(obj -> obj.render(canvas));
    }

    @Override
    public void resize(int width, int height) {
        int min = Math.min(width, height);

        int size = min / 3;
        int startMargin = (width < height)
                ? 0
                : (width - height) / 2;
        int topMargin = (width < height)
                ? (height - width) / 2
                : 0;

        squareGenerator.setFixSize(size, size);

        // Создаём новую группу блоков с новыми размерами
        Group newGrid = gridConstruct(startMargin, topMargin, size);
        // Переносим старые состояния
        int count = grid.size();
        for (int i = 0; i < count; i++) {
            Block oldBlock = (Block) grid.getObjects().get(i);
            Block newBlock = (Block) newGrid.getObjects().get(i);
            newBlock.mark(oldBlock.getState());
        }
        grid = newGrid;
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
