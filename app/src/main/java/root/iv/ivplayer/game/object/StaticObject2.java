package root.iv.ivplayer.game.object;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import lombok.Getter;
import root.iv.ivplayer.game.object.simple.Object2;
import root.iv.ivplayer.game.object.simple.Point2;


@Getter
public class StaticObject2 extends Object2 implements DrawableObject {
    protected Drawable drawable;
    protected int width;
    protected int height;

    public StaticObject2(Point2 position, Drawable drawable, int w, int h) {
        super(position);

        this.drawable = drawable;
        this.width = w;
        this.height = h;
    }


    public void render(Canvas canvas) {
        int x0 = Math.round(position.x);
        int y0 = Math.round(position.y);

        drawable.setBounds(x0, y0, x0+width, y0+height);
        drawable.draw(canvas);
    }

}
