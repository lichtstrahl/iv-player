package root.iv.ivplayer.game.fanorona.textures;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import root.iv.ivplayer.R;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChipTextures {
    private @ColorInt int whiteChipColor;
    private @ColorInt int blackChipColor;
    private Drawable whiteChip;
    private Drawable blackChip;

    public static ChipTextures create(Context context) {
        return new ChipTextures(
                Color.WHITE, Color.BLACK,
                context.getDrawable(R.drawable.ic_dog),
                context.getDrawable(R.drawable.ic_cat)
        );
    }
}
