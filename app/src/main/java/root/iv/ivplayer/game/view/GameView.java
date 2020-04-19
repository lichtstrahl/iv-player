package root.iv.ivplayer.game.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import root.iv.ivplayer.game.scene.Scene;

public class GameView extends View {
    @Nullable
    private Scene scene;

    public GameView(Context context) {
        super(context);
    }

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (scene != null) scene.render(canvas);

        invalidate();
    }

    public void loadScene(Scene scene) {
        this.scene = scene;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }
}
