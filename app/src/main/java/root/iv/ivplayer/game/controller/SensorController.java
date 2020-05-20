package root.iv.ivplayer.game.controller;

import android.view.MotionEvent;
import android.view.View;

import androidx.core.util.Consumer;

import root.iv.ivplayer.game.object.simple.Object2;

/**
 * Контроллер касаний:
 * 1. Может реагировать на нажатие на экран
 * 2. Событие прикосновений (тоже нажатие но с доп. информацией)
 */
public interface SensorController extends View.OnClickListener, View.OnTouchListener {
    void setClickHandler(Consumer<View> handler);
    void setTouchHandler(Consumer<MotionEvent> event);
}
