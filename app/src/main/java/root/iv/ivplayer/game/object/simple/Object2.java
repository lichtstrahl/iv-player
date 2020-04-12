package root.iv.ivplayer.game.object.simple;


import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class Object2 {
    protected Point2 position;
    protected boolean visible;

    public Object2(Point2 position) {
        this.position = position;
        visible = true;
    }
}
