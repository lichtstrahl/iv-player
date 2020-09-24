package root.iv.ivplayer.ui.fragment.menu;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import root.iv.ivplayer.R;
import root.iv.ivplayer.game.GameType;

public class MenuFragment extends Fragment {
    public static final String TAG = "fragment:menu";

    private Listener listener;

    @BindView(R.id.buttonAuth)
    protected MaterialButton buttonAuth;

    public static MenuFragment getInstance() {
        MenuFragment fragment = new MenuFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof Listener)
            listener = (Listener) context;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        buttonAuth.setEnabled(Objects.isNull(user));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @OnClick(R.id.buttonSinglePlay)
    public void clickSinglePlay() {
        // Открыть игровой фрагмент
        listener.startSingleGame(GameType.FANORONA);
    }

    @OnClick(R.id.buttonNetworkPlay)
    public void clickNetworkPlay() {
        listener.startNetworkGame();
    }

    @OnClick(R.id.buttonAuth)
    public void clickAuth() {
        listener.buttonAuthClick();
    }

    @OnClick(R.id.buttonStartService)
    public void clickStartService() {
        listener.buttonStartServiceClick();
    }

    public interface Listener {
        void startSingleGame(GameType gameType);
        void startNetworkGame();
        void buttonAuthClick();
        void buttonStartServiceClick();
    }
}
