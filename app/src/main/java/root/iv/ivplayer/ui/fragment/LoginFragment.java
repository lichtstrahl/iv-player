package root.iv.ivplayer.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

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
import root.iv.ivplayer.network.http.dto.server.AuthResponse;
import root.iv.ivplayer.network.http.dto.server.BaseResponse;
import root.iv.ivplayer.network.http.dto.server.UserEntityDTO;
import root.iv.ivplayer.network.ws.WSHolder;
import root.iv.ivplayer.network.ws.WSUtil;
import timber.log.Timber;

public class LoginFragment extends Fragment {
    private static final String SHARED_LOGIN_KEY = "shared:login";

    @BindView(R.id.inputLogin)
    protected TextInputEditText inputLogin;
    @BindView(R.id.inputPassword)
    protected TextInputEditText inputPassword;
    @BindView(R.id.buttonEnter)
    protected MaterialButton buttonEnter;
    @BindView(R.id.switchWS)
    protected SwitchMaterial switchWS;
    @BindView(R.id.buttonSend)
    protected MaterialButton buttonSend;

    private CompositeDisposable compositeDisposable;
    private Listener listener;
    private WSHolder wsHolder;

    public static LoginFragment getInstance() {
        return new LoginFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, view);

        compositeDisposable = new CompositeDisposable();
        wsHolder = WSHolder.fromURL(WSUtil.springWSURL("/ws/tic-tac", true));
        switchWS.setOnCheckedChangeListener(this::switchWS);

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            listener = (Listener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        compositeDisposable.dispose();
        listener = null;
    }

    @OnClick(R.id.buttonEnter)
    protected void clickEnter(View button) {
        String login = (inputLogin.getText() != null)
                ? inputLogin.getText().toString()
                : "";

        String password = (inputPassword.getText() != null)
                ? inputPassword.getText().toString()
                : "";
        Disposable disposable = App.getUserAPI().auth(login, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::processAuth, Timber::e);
        compositeDisposable.add(disposable);
    }

    @OnClick(R.id.buttonSend)
    protected void clickSend() {
        String msg = (inputLogin.getText() != null)
                ? inputLogin.getText().toString()
                : "";
        wsHolder.send(msg);
    }

    private void switchWS(View view, boolean isChecked) {

        if (isChecked) {
            wsHolder.open(string -> {
                String msg = String.format("From ws: %s", string);
                Timber.i(msg);
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            });
        } else {
            wsHolder.close();
        }
    }

    private void processAuth(BaseResponse<AuthResponse> response) {
        if (response.getErrorCode() == 0) {
            Objects.requireNonNull(response.getData());
            listener.authSuccessful(response.getData().getUser());
        } else {
            Timber.e(response.getErrorMsg());
        }
    }

    public interface Listener {
        void authSuccessful(UserEntityDTO user);
    }
}
