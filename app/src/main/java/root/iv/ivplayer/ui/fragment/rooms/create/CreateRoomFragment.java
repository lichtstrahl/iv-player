package root.iv.ivplayer.ui.fragment.rooms.create;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import lombok.AllArgsConstructor;
import root.iv.ivplayer.R;
import root.iv.ivplayer.game.GameType;
import root.iv.ivplayer.game.room.RoomState;
import root.iv.ivplayer.network.firebase.FBDataListener;
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

        int selectedChipId = gameTypeGroup.getCheckedChipId();

        if (!newRoomName.isEmpty() && selectedChipId != View.NO_ID) {
            GameType gameType = GameType.TIC_TAC;

            switch (selectedChipId) {
                case R.id.gameTypeFanorona:
                    gameType = GameType.FANORONA;
                    break;

                case R.id.gameTypeTicTac:
                    gameType = GameType.TIC_TAC;
                    break;
            }

            FBDatabaseAdapter.getRooms().addListenerForSingleValueEvent(new CreateRoomListener(newRoomName, gameType));

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

    // Смотрим какие сейчас комнаты есть и если нужное нам имя не занято, создаём
    @AllArgsConstructor
    private class CreateRoomListener extends FBDataListener {
        private String roomName;
        private GameType gameType;

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            boolean busy = false;
            for (DataSnapshot room : dataSnapshot.getChildren())
                if (room.getKey() != null && room.getKey().equals(roomName))
                    busy = true;

            if (!busy) {
                FBDatabaseAdapter.getRooms()
                        .child(roomName)
                        .child("state")
                        .setValue(RoomState.WAIT_PLAYERS);

                FBDatabaseAdapter.getRooms()
                        .child(roomName)
                        .child("gameType")
                        .setValue(gameType);

                CreateRoomFragment.this.getActivity().onBackPressed();
            } else {
                Toast.makeText(CreateRoomFragment.this.getContext(), "Название занято", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
