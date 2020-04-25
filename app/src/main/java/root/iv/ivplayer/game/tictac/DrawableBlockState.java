package root.iv.ivplayer.game.tictac;

import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DrawableBlockState {
    private Drawable cross;
    private Drawable circle;

    public static DrawableBlockState create(Drawable cross, Drawable circle) {
        return new DrawableBlockState(cross, circle);
    }

    @Nullable
    public Drawable getIcon(@Nullable BlockState state) {
        if (state == BlockState.CROSS) return cross;
        if (state == BlockState.CIRCLE) return circle;
        return null;
    }
}
