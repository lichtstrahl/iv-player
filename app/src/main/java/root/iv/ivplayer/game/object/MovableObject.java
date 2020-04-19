package root.iv.ivplayer.game.object;

public interface MovableObject  {
    void moveOn(int dx, int dy);
    void moveTo(int x, int y);
    void moveOn(float dx, float dy);
    void moveTo(float x, float y);
}
