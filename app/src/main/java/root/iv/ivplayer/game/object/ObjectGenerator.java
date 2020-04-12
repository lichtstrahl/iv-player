package root.iv.ivplayer.game.object;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;

import root.iv.ivplayer.game.object.simple.Point2;

public class ObjectGenerator {
    private Drawable drawable;
    private int width;
    private int height;

    public void setDrawable(Context context, @DrawableRes int id) {
        drawable = context.getResources().getDrawable(
                id,
                context.getTheme()
        );
    }

    public void setFixSize(int w, int h) {
        width = w;
        height = h;
    }

    public StaticObject2 buildStatic(int x0, int y0) {
        return new StaticObject2(Point2.point(x0, y0), drawable, width, height);
    }

    public Actor buildActor(int x0, int y0) {
        return new Actor(Point2.point(x0, y0), drawable, width, height);
    }
}
