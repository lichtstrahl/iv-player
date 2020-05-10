package root.iv.ivplayer.ui.fragment;

import android.icu.util.Calendar;
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
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import root.iv.ivplayer.R;
import root.iv.ivplayer.app.App;
import root.iv.ivplayer.network.http.dto.server.RoomEntityDTO;
import timber.log.Timber;

public class RoomsFragment extends Fragment {

    @BindView(R.id.cardRoom)
    protected MaterialCardView cardRoom;
    @BindView(R.id.viewRoomName)
    protected MaterialTextView viewRoomName;
    @BindView(R.id.viewRoomCreateDate)
    protected MaterialTextView viewRoomCreateDate;
    @BindView(R.id.viewRoomLive)
    protected MaterialCheckBox viewRoomLive;

    private CompositeDisposable compositeDisposable;

    public static RoomsFragment getInstance() {
        return new RoomsFragment();
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
    }

    @OnClick(R.id.buttonRefresh)
    protected void clickRefresh() {
        refreshRooms();
    }

    private void refreshRooms() {
        Disposable d = App.getRoomAPI().getAllRooms()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rooms -> {
                    RoomEntityDTO room = rooms.get(0);
                    viewRoomLive.setChecked(room.isLive());
                    viewRoomCreateDate.setText(stringDateTime(room.getCreateDate()));
                    viewRoomName.setText(room.getName());
                }, Timber::e);

        compositeDisposable.add(d);
    }

    private String stringDateTime(long milliseconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        return stringDateTime(calendar);
    }

    private String stringDateTime(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int m = calendar.get(Calendar.MINUTE);
        int s = calendar.get(Calendar.SECOND);
        return String.format("%s-%s-%s %s:%s:%s", year, month, day, h, m, s);
    }
}
