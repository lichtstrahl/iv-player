package root.iv.ivplayer.game.controller;

import android.view.MotionEvent;
import android.view.View;

import androidx.core.util.Consumer;

import root.iv.ivplayer.game.object.simple.Object2;

/**
 * Контроллер может брать под управление какой-либо объект
 * Может отпускать его и сообщать информацию о текущем состоянии (занят/свободен)
 */
public interface Controller extends View.OnClickListener, View.OnTouchListener {
    void grabObject(Object2 object);
    void releaseObject();
    boolean isReleased();
    void setClickHandler(Consumer<View> handler);
    void setTouchHandler(Consumer<MotionEvent> event);
}
