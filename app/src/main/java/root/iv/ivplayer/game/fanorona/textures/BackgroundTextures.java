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
public class BackgroundTextures {
    @Nullable
    private Drawable background;
    private @ColorInt int color;

    public static BackgroundTextures create(Context context) {
        return new BackgroundTextures(
                context.getDrawable(R.drawable.background_texture_of_dark_wood),
                Color.GRAY
        );
    }
}
