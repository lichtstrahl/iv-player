package root.iv.ivplayer.game.object;

import android.graphics.drawable.Drawable;

import lombok.Getter;
import root.iv.ivplayer.game.object.simple.Point2;

// Игрок - тоже актёр, но он имеет свой UUID
@Getter
public class Player extends Actor {
    private String uuid;

    public Player(Actor actor, String uuid) {
        super(actor.getPosition(), actor.getDrawable(), actor.getWidth(), actor.getHeight());
        this.uuid = uuid;
    }

    public Player(Point2 position, Drawable drawable, int w, int h, String uuid) {
        super(position, drawable, w, h);
        this.uuid = uuid;
    }
}
