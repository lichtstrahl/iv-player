package root.iv.ivplayer.ui.fragment;

import android.content.Context;
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
import root.iv.ivplayer.network.http.dto.server.RoomEntityDTO;
import root.iv.ivplayer.util.DateTimeUtil;
import timber.log.Timber;

public class RoomsFragment extends Fragment {
    public static final String TAG = "fragment:rooms";
    private static final String ARG_LOGIN = "arg:login";

    @BindView(R.id.cardRoom)
    protected MaterialCardView cardRoom;
    @BindView(R.id.viewRoomName)
    protected MaterialTextView viewRoomName;
    @BindView(R.id.viewRoomCreateDate)
    protected MaterialTextView viewRoomCreateDate;
    @BindView(R.id.viewRoomLive)
    protected MaterialCheckBox viewRoomLive;

    private CompositeDisposable compositeDisposable;
    private Listener listener;

    public static RoomsFragment getInstance(String login) {
        RoomsFragment fragment = new RoomsFragment();

        Bundle bundle = new Bundle();
        bundle.putString(ARG_LOGIN, login);
        fragment.setArguments(bundle);

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
        Bundle args = Objects.requireNonNull(getArguments());
        listener.clickRoom(viewRoomName.getText().toString(), args.getString(ARG_LOGIN));
    }

    private void refreshRooms() {
        Disposable d = App.getRoomAPI().getAllRooms()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rooms -> {
                    RoomEntityDTO room = rooms.get(0);
                    viewRoomLive.setChecked(room.isLive());
                    viewRoomCreateDate.setText(DateTimeUtil.stringDateTime(room.getCreateDate()));
                    viewRoomName.setText(room.getName());
                }, Timber::e);

        compositeDisposable.add(d);
    }

    public interface Listener {
        void clickRoom(String roomName, String login);
    }
}
