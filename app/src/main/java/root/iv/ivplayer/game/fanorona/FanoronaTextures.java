package root.iv.ivplayer.game.fanorona;

import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
public class FanoronaTextures {
    // BACKGROUND
    private @ColorInt int backgroundColor;
    private Drawable background;

    // CHIP
    private @ColorInt int chipWhiteColor;
    private Drawable chipWhite;
    private @ColorInt int chipBlackColor;
    private Drawable chipBlack;

    // SLOT
    private @ColorInt int slotColor;
    private Drawable slot;

    public static FanoronaTextures create(Drawable background, Drawable chipWhite, Drawable chipBlack, Drawable slot) {
        return new FanoronaTextures(Color.GRAY, background, Color.WHITE, chipWhite, Color.BLACK, chipBlack, Color.BLACK, slot);
    }

    public static FanoronaTextures light(Drawable background, Drawable chipWhite, Drawable chipBlack, Drawable slot) {
        return new FanoronaTextures(Color.GRAY, background, Color.WHITE, chipWhite, Color.DKGRAY, chipBlack, Color.LTGRAY, slot);
    }
}

