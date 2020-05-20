package root.iv.ivplayer.game.controller;

import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

/**
 * Контроллер касаний:
 * 1. Может реагировать на нажатие на экран
 * 2. Событие прикосновений (тоже нажатие но с доп. информацией)
 */
public abstract class SensorController implements View.OnClickListener, View.OnTouchListener {

    @Nullable
    private Consumer<View> clickConsumer;
    @Nullable
    private Consumer<MotionEvent> touchConsumer;

    @Override
    public void onClick(View v) {
        if (clickConsumer != null) clickConsumer.accept(v);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (touchConsumer != null) touchConsumer.accept(event);
        return false;
    }

    public void setClickHandler(Consumer<View> handler) {
        clickConsumer = handler;
    }

    public void setTouchHandler(Consumer<MotionEvent> handler) {
        touchConsumer = handler;
    }
}
