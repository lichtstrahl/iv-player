package root.iv.ivplayer.ui.fragment.rooms;

import android.content.Context;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.CompositeDisposable;
import lombok.AllArgsConstructor;
import root.iv.ivplayer.R;
import root.iv.ivplayer.game.room.Room;
import root.iv.ivplayer.game.room.RoomState;
import root.iv.ivplayer.network.firebase.FBDatabaseAdapter;
import root.iv.ivplayer.network.firebase.FBDataListener;
import root.iv.ivplayer.network.firebase.dto.FBRoom;
import root.iv.ivplayer.network.firebase.dto.RoomUI;

public class RoomsFragment extends Fragment {
    private static final int MENU_ITEM_DELETE = 0;
    private static final int MENU_ITEM_REOPEN = 1;
    public static final String TAG = "fragment:rooms";

    @BindView(R.id.recyclerListRooms)
    protected RecyclerView recyclerListRooms;
    @BindView(R.id.inputNewRoomName)
    protected TextInputEditText inputNewRoomName;

    private CompositeDisposable compositeDisposable;
    private Listener listener;
    private RoomsAdapter roomsAdapter;
    private FirebaseUser fbCurrentUser;

    private RoomsFBListener roomsFBListener;

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
        roomsAdapter = RoomsAdapter.empty(inflater, this::clickRoom, this::createContextMenu);
        fbCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerListRooms.setAdapter(roomsAdapter);
        recyclerListRooms.setLayoutManager(new LinearLayoutManager(this.getContext(),RecyclerView.VERTICAL, false));

        roomsFBListener = new RoomsFBListener();

        registerForContextMenu(recyclerListRooms);
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        compositeDisposable.dispose();
        listener = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        FBDatabaseAdapter.getRooms()
                .removeEventListener(roomsFBListener);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Получаем список комнат: child-узлы поля rooms
        FBDatabaseAdapter.getRooms()
                .addValueEventListener(roomsFBListener);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof Listener)
            listener = (Listener) context;
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int position = roomsAdapter.getPosition();
        RoomUI room = roomsAdapter.getRoom(position);

        switch (item.getItemId()) {
            case MENU_ITEM_DELETE:
                FBDatabaseAdapter.getRooms().child(room.getName()).removeValue(this::deleteRoomListener);
                break;

            case MENU_ITEM_REOPEN:
                FBDatabaseAdapter.getRooms().child(room.getName()).removeValue();
                FBDatabaseAdapter.getRoomStatus(room.getName()).setValue(RoomState.WAIT_PLAYERS);
                break;
        }
        return super.onContextItemSelected(item);
    }

    @OnClick(R.id.buttonCreateRoom)
    protected void clickButtonCreateRoom() {
        String newRoomName = (inputNewRoomName.getText() != null)
                ? inputNewRoomName.getText().toString()
                : "";
        if (!newRoomName.isEmpty()) {
            FBDatabaseAdapter.getRooms().addListenerForSingleValueEvent(new CreateRoomListener(newRoomName));
        } else {
            Toast.makeText(this.getContext(), "Имя не задано", Toast.LENGTH_SHORT).show();
        }
    }

    private void clickRoom(View roomItemView) {
        int position = recyclerListRooms.getChildAdapterPosition(roomItemView);
        RoomUI room = roomsAdapter.getRoom(position);
        // При нажатии на кнопку необходимо обновить
        FBDatabaseAdapter.getRoom(room.getName())
                .addListenerForSingleValueEvent(new EnterRoomListener(room.getName(), fbCurrentUser.getEmail()));
    }

    // Если в комнате нет игроков, то для неё возможен вызов контекстного меню
    private void createContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("title");
        int position = recyclerListRooms.getChildAdapterPosition(view);
        RoomUI roomUI = roomsAdapter.getRoom(position);

        if (roomUI.countPlayer() == 0) {
            menu.add(Menu.NONE, MENU_ITEM_DELETE, Menu.NONE, "delete");

            if (roomUI.getState() == RoomState.CLOSE) {
                menu.add(Menu.NONE, MENU_ITEM_REOPEN, Menu.NONE, "reopen");
            }
        }
    }

    public interface Listener {
        void clickRoom(String roomName);
    }

    // Реагируем на изменение списка комнат и данных в них
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

    // Смотрим какие сейчас комнаты есть и если нужное нам имя не занято, создаём
    @AllArgsConstructor
    private class CreateRoomListener extends FBDataListener {
        private String roomName;

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
            } else {
                Toast.makeText(RoomsFragment.this.getContext(), "Название занято", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteRoomListener(@Nullable DatabaseError error, @NonNull DatabaseReference reference) {
        if (error == null) {
            roomsAdapter.removeRoom(reference.getKey());
        }
    }

    // Клик на элемент списка, вход в комнату если это возможно
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
