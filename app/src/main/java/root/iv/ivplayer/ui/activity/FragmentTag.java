package root.iv.ivplayer.ui.activity;

import androidx.fragment.app.Fragment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FragmentTag {
    private String tag;
    private Fragment fragment;
}
