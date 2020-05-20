package root.iv.ivplayer.game.tictac;

import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import root.iv.ivplayer.game.controller.SensorController;
import root.iv.ivplayer.game.object.simple.Object2;

public class TicTacController implements SensorController {
    @Nullable
    private Consumer<View> clickConsumer;
    @Nullable
    private Consumer<MotionEvent> touchConsumer;

    @Override
    public void grabObject(Object2 object) {
    }

    @Override
    public void releaseObject() {
    }

    @Override
    public boolean isReleased() {
        return true;
    }

    @Override
    public void onClick(View v) {
        if (clickConsumer != null) clickConsumer.accept(v);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (touchConsumer != null) touchConsumer.accept(event);
        return false;
    }

    @Override
    public void setClickHandler(Consumer<View> handler) {
        clickConsumer = handler;
    }

    @Override
    public void setTouchHandler(Consumer<MotionEvent> handler) {
        touchConsumer = handler;
    }
}
