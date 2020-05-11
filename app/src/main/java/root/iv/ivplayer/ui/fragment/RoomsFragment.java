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
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import root.iv.ivplayer.R;
import root.iv.ivplayer.app.App;
import root.iv.ivplayer.network.firebase.dto.FBRoom;
import root.iv.ivplayer.network.http.dto.server.RoomEntityDTO;
import root.iv.ivplayer.util.DateTimeUtil;
import timber.log.Timber;

public class RoomsFragment extends Fragment {
    public static final String TAG = "fragment:rooms";

    @BindView(R.id.cardRoom)
    protected MaterialCardView cardRoom;
    @BindView(R.id.viewRoomName)
    protected MaterialTextView viewRoomName;
    @BindView(R.id.viewEmailPlayer1)
    protected MaterialTextView viewEmailPlayer1;
    @BindView(R.id.viewEmailPlayer2)
    protected MaterialTextView viewEmailPlayer2;

    private CompositeDisposable compositeDisposable;
    private Listener listener;

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
    }

    private void refreshRooms() {
        // Получаем список комнат: child-узлы поля rooms
        App.getFbDatabase()
                .getReference("rooms")
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
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Timber.e(databaseError.getMessage());
        }
    }

    public interface Listener {
        void clickRoom(String roomName, String login);
    }
}
