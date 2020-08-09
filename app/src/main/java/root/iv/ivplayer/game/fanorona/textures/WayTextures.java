package root.iv.ivplayer.game.fanorona.textures;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import root.iv.ivplayer.R;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WayTextures {
    private @ColorInt int originColor;
    private int[] usedColor;
    private double alpha;
    @Nullable
    private Drawable drawable;

    public static WayTextures create(Context context) {
        int[] colorsPower = new int[] {
                context.getColor(R.color.way_used_power_1),
                context.getColor(R.color.way_used_power_2),
                context.getColor(R.color.way_used_power_3)
        };

        return new WayTextures(
                Color.WHITE,
                colorsPower,
                0.6,
                null
        );
    }
}
