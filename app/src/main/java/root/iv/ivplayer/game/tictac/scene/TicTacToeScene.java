package root.iv.ivplayer.game.tictac.scene;

import android.graphics.Canvas;
import android.graphics.RectF;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import root.iv.ivplayer.game.controller.SensorController;
import root.iv.ivplayer.game.object.DrawableObject;
import root.iv.ivplayer.game.object.Group;
import root.iv.ivplayer.game.object.ObjectGenerator;
import root.iv.ivplayer.game.object.StaticObject2;
import root.iv.ivplayer.game.scene.SensorScene;
import root.iv.ivplayer.game.tictac.Block;
import root.iv.ivplayer.game.tictac.BlockState;
import root.iv.ivplayer.game.tictac.TicTacController;
import root.iv.ivplayer.game.tictac.TicTacTextures;
import root.iv.ivplayer.game.tictac.dto.TicTacProgressDTO;

public class TicTacToeScene extends SensorScene {

    // Генераторы для создания объектов
    private ObjectGenerator squareGenerator;
    private ObjectGenerator backgroundGenerator;
    private TicTacTextures textures;

    // Объекты для отрисовки
    private List<DrawableObject> drawableObjects;
    private Group<Block> grid;

    public TicTacToeScene(TicTacTextures textures, int squareSize, int startMargin, int topMargin) {
        super(new TicTacController());
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
    }

    public List<Block> getAllBlocks() {
        return this.grid.getObjects();
    }

    private Group<Block> gridConstruct(int startMargin, int topMargin, int squareSize) {
        Group<Block> group = Group.empty();
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
        Group<Block> newGrid = gridConstruct(startMargin, topMargin, size);
        // Переносим старые состояния
        int count = grid.size();
        for (int i = 0; i < count; i++) {
            newGrid.getObjects().get(i)
                    .mark(grid.getObjects().get(i).getState());
        }
        grid = newGrid;
    }

    @Nullable
    public Integer touchUpBlock(float x, float y) {
        for (int i = 0; i < grid.size(); i++) {
            Block b = grid.getObject(i);
            RectF bounds = b.getBounds();
            boolean click = bounds.contains(x, y);
            if (click && b.getState() == BlockState.FREE) {
                return i;
            }
        }

        return null;
    }
}
