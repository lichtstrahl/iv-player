package root.iv.ivplayer.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
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
import root.iv.ivplayer.R;
import root.iv.ivplayer.app.App;
import root.iv.ivplayer.network.firebase.dto.FBRoom;
import timber.log.Timber;

public class RoomsFragment extends Fragment {
    public static final String TAG = "fragment:rooms";

    @BindView(R.id.cardRoom)
    protected MaterialCardView cardRoom;
    @BindView(R.id.viewRoomName)
    protected MaterialTextView viewRoomName;
    @BindView(R.id.viewRoomState)
    protected MaterialTextView viewRoomState;
    @BindView(R.id.viewEmailPlayer1)
    protected MaterialTextView viewEmailPlayer1;
    @BindView(R.id.viewEmailPlayer2)
    protected MaterialTextView viewEmailPlayer2;

    private CompositeDisposable compositeDisposable;
    private Listener listener;
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
        fbCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

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

    @OnClick(R.id.buttonRefresh)
    protected void clickRefresh() {
        refreshRooms();
    }

    @OnClick(R.id.cardRoom)
    protected void clickRoom() {
        // При нажатии на кнопку необходимо обновить
        App.getRoom(viewRoomName.getText().toString())
                .addListenerForSingleValueEvent(new EnterRoomListener());
    }

    private void refreshRooms() {
        // Получаем список комнат: child-узлы поля rooms
        App.getRooms()
                .addValueEventListener(new RoomsFBListener());

    }


    private class RoomsFBListener implements ValueEventListener {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            long count = dataSnapshot.getChildrenCount();
            if (count == 0)
                Toast.makeText(RoomsFragment.this.getActivity(), "Комнат нет", Toast.LENGTH_SHORT).show();

            for (DataSnapshot room : dataSnapshot.getChildren()) {
                String roomName = room.getKey();
                FBRoom fbRoom = Objects.requireNonNull(room.getValue(FBRoom.class));
                viewRoomName.setText(roomName);
                viewEmailPlayer1.setText(fbRoom.getEmailPlayer1());
                viewEmailPlayer2.setText(fbRoom.getEmailPlayer2());
                viewRoomState.setText(fbRoom.getState().name());
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Timber.e(databaseError.getMessage());
        }
    }

    public interface Listener {
        void clickRoom(String roomName);
    }

    private class EnterRoomListener implements ValueEventListener {

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            FBRoom room = dataSnapshot.getValue(FBRoom.class);
            // Если оба заняты, то ничего не делаем
            if (!room.getEmailPlayer1().isEmpty() && !room.getEmailPlayer2().isEmpty()) {
                Toast.makeText(RoomsFragment.this.getActivity(), "Места заняты", Toast.LENGTH_SHORT).show();
                return;
            }

            // Заполняем email-ы (если 1 занят, пишем себя во второй)
            if (room.getEmailPlayer1().isEmpty()) {
                room.setEmailPlayer1(fbCurrentUser.getEmail());
            } else if (room.getEmailPlayer2().isEmpty()) {
                room.setEmailPlayer2(fbCurrentUser.getEmail());
            }

            App.getRoom(viewRoomName.getText().toString())
                    .setValue(room);


            listener.clickRoom(viewRoomName.getText().toString());
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Timber.w(databaseError.getMessage());
        }
    }
}
