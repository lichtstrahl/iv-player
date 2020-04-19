package root.iv.ivplayer.game.controller;

import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import root.iv.ivplayer.game.object.MovableObject;
import root.iv.ivplayer.game.object.simple.Object2;
import timber.log.Timber;

// Контроллер, служит для управления каким-либо объектом
// Для этого он как минимум должен быть движимым
public class MoveController implements View.OnClickListener,Controller {
    @Nullable
    private MovableObject object;

    @Override
    public void onClick(View v) {
        if (object != null) {
            object.moveOn(0, 10);
        }
    }

    @Override
    public void grabObject(Object2 object) {
        if (object instanceof MovableObject) {
            this.object = (MovableObject) object;
        } else {
            Timber.e("В контроллер движения кладётся неподдерживаемый объект");
        }
    }

    @Override
    public void releaseObject() {
        this.object = null;
    }

    @Override
    public boolean isReleased() {
        return object == null;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
