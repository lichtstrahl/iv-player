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
public class WayTextures {
    private @ColorInt int originColor;
    private @ColorInt int usedColor;
    private double alpha;
    @Nullable
    private Drawable drawable;

    public static WayTextures create(Context context) {
        return new WayTextures(
                Color.WHITE,
                Color.BLUE,
                0.6,
                null
        );
    }
}
