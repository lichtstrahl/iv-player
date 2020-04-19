package root.iv.ivplayer.game.tictac;

import android.view.MotionEvent;
import android.view.View;

import androidx.core.util.Consumer;

import lombok.AllArgsConstructor;
import root.iv.ivplayer.game.controller.Controller;
import root.iv.ivplayer.game.object.simple.Object2;

@AllArgsConstructor
public class TicTacController implements Controller {
    private Consumer<View> clickConsumer;
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
        clickConsumer.accept(v);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        touchConsumer.accept(event);
        return false;
    }
}
