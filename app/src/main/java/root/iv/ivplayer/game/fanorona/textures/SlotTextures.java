package root.iv.ivplayer.game.fanorona.textures;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SlotTextures {
    private @ColorInt int freeColor;
    private @ColorInt int selectedColor;
    private @ColorInt int progressColor;
    private @ColorInt int hasProgressColor;
    private double alpha;
    @Nullable
    private Drawable drawable;

    public static SlotTextures create(Context context) {
        return new SlotTextures(
                Color.WHITE,
                Color.YELLOW,
                Color.RED,
                Color.GREEN,
                0.7,
                null
        );
    }
}
