package root.iv.ivplayer.game.object;

import android.graphics.drawable.Drawable;

import lombok.Getter;
import root.iv.ivplayer.game.object.simple.Point2;

// Класс актера. Он обладает всем свойствами статичного объекта.
// При этом обладает вощможностью двигаться
@Getter
public class Actor extends StaticObject2 implements MovableObject {

    public Actor(Point2 position, Drawable drawable, int w, int h) {
        super(position, drawable, w, h);
    }

    @Override
    public void moveOn(int dx, int dy) {
        position.x += dx;
        position.y += dy;
    }

    @Override
    public void moveTo(int x, int y) {
        position.x = x;
        position.y = y;
    }

    @Override
    public void moveOn(float dx, float dy) {
        position.x += dx;
        position.y += dy;
    }

    @Override
    public void moveTo(float x, float y) {
        position.x = x;
        position.y = y;
    }
}
