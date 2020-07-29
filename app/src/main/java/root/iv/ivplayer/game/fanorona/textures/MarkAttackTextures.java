package root.iv.ivplayer.game.fanorona.textures;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.ColorInt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class MarkAttackTextures {
    private @ColorInt int color;
    private double alpha;

    // По умолчанию фишки будут зачеркиваться полупрозрачным красным крестом
    public static MarkAttackTextures defaultMark(Context context) {
        return new MarkAttackTextures(Color.RED, 0.6);
    }
}
