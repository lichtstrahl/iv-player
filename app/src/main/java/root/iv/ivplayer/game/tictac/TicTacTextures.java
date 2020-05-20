package root.iv.ivplayer.game.tictac;

import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import lombok.Builder;
import lombok.Data;

// Игра крестик-нолик
// Класс для хранения текстур
@Data
@Builder
public class TicTacTextures {
    // CIRCLE
    private @ColorInt int circleColor;
    private Drawable circle;
    // CROSS
    private @ColorInt int crossColor;
    private Drawable cross;
    // SQUARE
    private @ColorInt int squareColor;
    private Drawable square;
    // BACKGROUND
    private @ColorInt int backgroundColor;
    @Nullable
    private Drawable background;

    public static TicTacTextures create(Drawable square, Drawable circle, Drawable cross, @ColorInt int color, Drawable background) {
        return TicTacTextures
                .builder()
                .circleColor(color)
                .circle(circle)
                .crossColor(color)
                .cross(cross)
                .squareColor(color)
                .square(square)
                .background(background)
                .build();
    }
}
