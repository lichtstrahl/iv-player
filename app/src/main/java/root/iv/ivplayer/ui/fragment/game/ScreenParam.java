package root.iv.ivplayer.ui.fragment.game;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScreenParam implements Serializable {
    private boolean fullScreen;
    private boolean visibleActionBar;
    private int orientation;
}
