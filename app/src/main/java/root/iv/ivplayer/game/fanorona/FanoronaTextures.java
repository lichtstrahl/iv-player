package root.iv.ivplayer.game.fanorona;

import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FanoronaTextures {
    // BACKGROUND
    private @ColorInt int backgroundColor;
    private Drawable background;

    // CHIP
    @Nullable
    private @ColorInt Integer chipWhiteColor;
    private Drawable chipWhite;
    @Nullable
    private @ColorInt Integer chipBlackColor;
    private Drawable chipBlack;

    // SLOT
    private @ColorInt int slotColor;
    private Drawable slot;

    public static FanoronaTextures create(Drawable background, Drawable chipWhite, Drawable chipBlack, Drawable slot) {
        return new FanoronaTextures(Color.GRAY, background, Color.WHITE, chipWhite, Color.BLACK, chipBlack, Color.BLACK, slot);
    }

    public static FanoronaTextures light(Drawable background, Drawable chipWhite, Drawable chipBlack, Drawable slot) {
        return new FanoronaTextures(Color.GRAY, background, Color.WHITE, chipWhite, null, chipBlack, Color.LTGRAY, slot);
    }
}

