package root.iv.ivplayer.game.fanorona.textures;

import android.content.Context;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(access = AccessLevel.PRIVATE)
public class FanoronaTextures {
    // BACKGROUND
    private BackgroundTextures backgroundTextures;

    // CHIP
    private ChipTextures chipTextures;

    // SLOT
    private SlotTextures slotTextures;

    // WAY
    private WayTextures wayTextures;

    public static FanoronaTextures create(Context context) {
        return FanoronaTextures
                .builder()
                .backgroundTextures(BackgroundTextures.create(context))
                .chipTextures(ChipTextures.create(context))
                .slotTextures(SlotTextures.create(context))
                .wayTextures(WayTextures.create(context))
                .build();
    }
}

