package root.iv.ivplayer.ui.fragment.rooms;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.CompositeDisposable;
import lombok.AllArgsConstructor;
import root.iv.ivplayer.R;
import root.iv.ivplayer.app.App;
import root.iv.ivplayer.game.room.RoomState;
import root.iv.ivplayer.network.firebase.FBDatabaseAdapter;
import root.iv.ivplayer.network.firebase.FBDataListener;
import root.iv.ivplayer.network.firebase.dto.FBRoom;
import root.iv.ivplayer.network.firebase.dto.RoomUI;
import timber.log.Timber;

public class RoomsFragment extends Fragment {
    public static final String TAG = "fragment:rooms";

    @BindView(R.id.recyclerListRooms)
    protected RecyclerView recyclerListRooms;
    @BindView(R.id.inputNewRoomName)
    protected TextInputEditText inputNewRoomName;

    private CompositeDisposable compositeDisposable;
    private Listener listener;
    private RoomsAdapter roomsAdapter;
    private FirebaseUser fbCurrentUser;

    public static RoomsFragment getInstance(String login) {
        RoomsFragment fragment = new RoomsFragment();

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rooms, container, false);
        ButterKnife.bind(this, view);

        compositeDisposable = new CompositeDisposable();
        refreshRooms();
        roomsAdapter = RoomsAdapter.empty(inflater, this::clickRoom);
        fbCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerListRooms.setAdapter(roomsAdapter);
        recyclerListRooms.setLayoutManager(new LinearLayoutManager(this.getContext(),RecyclerView.VERTICAL, false));
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        compositeDisposable.dispose();
        listener = null;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof Listener)
            listener = (Listener) context;
    }

    @OnClick(R.id.buttonCreateRoom)
    protected void clickButtonCreateRoom() {
        String newRoomName = (inputNewRoomName.getText() != null)
                ? inputNewRoomName.getText().toString()
                : "";
        if (!newRoomName.isEmpty()) {
            FBDatabaseAdapter.getRooms()
                    .child(newRoomName)
                    .child("state")
                    .setValue(RoomState.WAIT_PLAYERS);
        } else {
            Toast.makeText(this.getContext(), "Имя не задано", Toast.LENGTH_SHORT);
        }
    }

    protected void clickRoom(View roomItemView) {
        int position = recyclerListRooms.getChildAdapterPosition(roomItemView);
        RoomUI room = roomsAdapter.getRoom(position);
        // При нажатии на кнопку необходимо обновить
        FBDatabaseAdapter.getRoom(room.getName())
                .addListenerForSingleValueEvent(new EnterRoomListener(room.getName(), fbCurrentUser.getEmail()));
    }

    private void refreshRooms() {
        // Получаем список комнат: child-узлы поля rooms
        FBDatabaseAdapter.getRooms()
                .addValueEventListener(new RoomsFBListener());
    }

    public interface Listener {
        void clickRoom(String roomName);
    }

    private class RoomsFBListener extends FBDataListener {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            long count = dataSnapshot.getChildrenCount();
            if (count == 0)
                Toast.makeText(RoomsFragment.this.getActivity(), "Комнат нет", Toast.LENGTH_SHORT).show();

            for (DataSnapshot room : dataSnapshot.getChildren()) {
                String roomName = room.getKey();
                FBRoom fbRoom = Objects.requireNonNull(room.getValue(FBRoom.class));
                roomsAdapter.roomNotify(roomName, fbRoom);
            }
        }
    }

    @AllArgsConstructor
    private class EnterRoomListener extends FBDataListener {
        private String roomName;
        private String email;

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            FBRoom room = dataSnapshot.getValue(FBRoom.class);

            if (room.getState() != RoomState.WAIT_PLAYERS) {
                Toast.makeText(RoomsFragment.this.getActivity(), "Комната не ждёт игроков", Toast.LENGTH_SHORT).show();
                return;
            }

            // Заполняем email-ы (если 1 занят, пишем себя во второй)
            if (room.getEmailPlayer1() == null || room.getEmailPlayer1().isEmpty()) {
                room.setEmailPlayer1(email);
            } else if (room.getEmailPlayer2() == null || room.getEmailPlayer2().isEmpty()) {
                room.setEmailPlayer2(email);
            }

            FBDatabaseAdapter.getRoom(roomName)
                    .setValue(room);


            listener.clickRoom(roomName);
        }
    }
}
