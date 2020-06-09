package root.iv.ivplayer.ui.fragment.game;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScreenParam {
    private boolean fullScreen;
    private boolean visibleActionBar;
    private int orientation;
}
