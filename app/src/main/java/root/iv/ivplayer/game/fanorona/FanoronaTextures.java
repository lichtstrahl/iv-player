package root.iv.ivplayer.game.fanorona;

import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FanoronaTextures {
    // BACKGROUND
    private @ColorInt int backgroundColor;
    private Drawable background;

    // CHIP
    private @ColorInt int chipColor;
    private Drawable chip;

    // SLOT
    private @ColorInt int slotColor;
    private Drawable slot;
}
