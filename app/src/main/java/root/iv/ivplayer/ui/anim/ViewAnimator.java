package root.iv.ivplayer.ui.anim;

import android.view.View;

import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;

public class ViewAnimator {

    public static void moveOn(View view, float toX, float toY) {
        SpringAnimation animX = new SpringAnimation(view, DynamicAnimation.TRANSLATION_X);
        SpringAnimation animY = new SpringAnimation(view, DynamicAnimation.TRANSLATION_Y);

        animX.animateToFinalPosition(toX);
        animY.animateToFinalPosition(toY);
    }
}
