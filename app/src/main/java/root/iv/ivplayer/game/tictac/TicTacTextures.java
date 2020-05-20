package root.iv.ivplayer.game.tictac;

import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;

import lombok.Builder;
import lombok.Data;

// Игра крестик-нолик
// Класс для хранения текстур
@Data
@Builder
public class TicTacTextures {
    private Drawable circle;
    private Drawable cross;
    private @ColorInt int background;
    private Drawable square;
}
