package root.iv.ivplayer.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import root.iv.ivplayer.R;

public class RoomsFragment extends Fragment {

    @BindView(R.id.cardRoom)
    protected MaterialCardView cardRoom;
    @BindView(R.id.viewRoomName)
    protected MaterialTextView viewRoomName;
    @BindView(R.id.viewRoomCreateDate)
    protected MaterialTextView viewRoomCreateDate;
    @BindView(R.id.viewRoomLive)
    protected MaterialCheckBox viewRoomLive;

    public static RoomsFragment getInstance() {
        return new RoomsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rooms, container, false);
        ButterKnife.bind(this, view);

        return view;
    }
}
