package root.iv.ivplayer.game.tictac;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import lombok.Getter;
import root.iv.ivplayer.game.object.ObjectGenerator;
import root.iv.ivplayer.game.object.StaticObject2;

// Блок сможет отрисовывать на себе другие объекты: крестик-нолик
@Getter
public class Block extends StaticObject2 {
    private static final int margin = 10;

    private BlockState state;
    private ObjectGenerator crossGenerator;
    private ObjectGenerator circleGenerator;

    private Block(StaticObject2 staticObject) {
        super(staticObject.getPosition(),
                staticObject.getDrawable(),
                staticObject.getWidth(),
                staticObject.getHeight());
        this.state = BlockState.FREE;
    }

    public static Block of(StaticObject2 block, Drawable cross, Drawable circle) {
        Block b =  new Block(block);

        int iconW = b.width-margin*2;
        int iconH = b.height-margin*2;

        // Генератор для крестиков
        b.crossGenerator = new ObjectGenerator();
        b.crossGenerator.setDrawable(cross);
        b.crossGenerator.setFixSize(iconW, iconH);

        // Генератор для ноликов
        b.circleGenerator = new ObjectGenerator();
        b.circleGenerator.setDrawable(circle);
        b.circleGenerator.setFixSize(iconW, iconH);

        return b;
    }

    public void mark(BlockState state) {
        this.state = state;
    }

    // После отрисовки самого блока в зависимости от состояния будет нарисована метка
    @Override
    public void render(Canvas canvas) {
        super.render(canvas);
        int x0 = Math.round(position.x) + margin;
        int y0 = Math.round(position.y) + margin;

        switch (state) {
            case CIRCLE:
                StaticObject2 circle = circleGenerator.buildStatic(x0, y0);
                circle.render(canvas);
                break;

            case CROSS:
                StaticObject2 cross = crossGenerator.buildStatic(x0,y0);
                cross.render(canvas);
                break;
        }
    }
}
