package root.iv.ivplayer.ui.fragment.rooms.create;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import root.iv.ivplayer.R;
import root.iv.ivplayer.network.firebase.FBDatabaseAdapter;
import root.iv.ivplayer.ui.fragment.rooms.list.RoomsFragment;

public class CreateRoomFragment extends Fragment {
    public static final String TAG = "fragment:room-create";

    @BindView(R.id.inputNewRoomName)
    protected TextInputEditText inputNewRoomName;
    @BindView(R.id.gameTypeGroup)
    protected ChipGroup gameTypeGroup;

    private Listener listener;

    public static CreateRoomFragment newInstance() {
        CreateRoomFragment fragment = new CreateRoomFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_room, container, false);
        ButterKnife.bind(this, view);


        gameTypeGroup.setOnCheckedChangeListener(this::setGameTypeChip);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            listener = (Listener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @OnClick(R.id.buttonCreateRoom)
    protected void clickButtonCreateRoom() {
        String newRoomName = (inputNewRoomName.getText() != null)
                ? inputNewRoomName.getText().toString()
                : "";
        if (!newRoomName.isEmpty()) {
            int selectedChipId = gameTypeGroup.getCheckedChipId();
            String game = "";

            switch (selectedChipId) {
                case R.id.gameTypeFanorona:
                    game = "FANORONA";
                    break;

                case R.id.gameTypeTicTac:
                    game = "TicTac";
                    break;
            }

            Toast.makeText(this.getContext(), "create room " + game, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this.getContext(), "Имя не задано", Toast.LENGTH_SHORT).show();
        }
    }

    private void setGameTypeChip(ChipGroup group, int viewID) {
        for (int i = 0; i < group.getChildCount(); i++) {
            Chip chip = (Chip)group.getChildAt(i);

            if (chip.getId() == viewID) {
                chip.setChecked(true);
                chip.setClickable(false);
            } else {
                chip.setChecked(false);
                chip.setClickable(true);
            }
        }
    }

    public interface Listener {
    }
}
