package root.iv.ivplayer.game.object;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import root.iv.ivplayer.game.object.simple.Point2;

public class ObjectGenerator {
    @Nullable
    private Drawable drawable;
    private int width;
    private int height;

    public void setDrawable(Context context, @DrawableRes int id) {
        drawable = context.getResources().getDrawable(
                id,
                context.getTheme()
        );
    }

    public void setDrawable(@Nullable Drawable drawable) {
        this.drawable = drawable;
    }

    public void setFixSize(int w, int h) {
        width = w;
        height = h;
    }

    public void setTintColor(@ColorInt int color) {
        if (drawable != null)
            drawable.setTint(color);
    }

    public boolean hasTexture() {
        return drawable != null;
    }

    public StaticObject2 buildStatic(int x0, int y0) {
        return new StaticObject2(Point2.point(x0, y0), drawable, width, height);
    }

    public Actor buildActor(int x0, int y0) {
        return new Actor(Point2.point(x0, y0), drawable, width, height);
    }

    public Player buildPlayer(int x0, int y0, String uuid) {
        return new Player(buildActor(x0, y0), uuid);
    }
}
