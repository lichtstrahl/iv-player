package root.iv.ivplayer.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.CompositeDisposable;
import root.iv.ivplayer.R;
import root.iv.ivplayer.network.http.dto.server.AuthResponse;
import root.iv.ivplayer.network.http.dto.server.BaseResponse;
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

    private CompositeDisposable compositeDisposable;
    private Listener listener;
    private FirebaseAuth fbAuth;

    public static LoginFragment getInstance() {
        return new LoginFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, view);

        compositeDisposable = new CompositeDisposable();
        fbAuth = FirebaseAuth.getInstance();


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
    public void onStart() {
        super.onStart();
        FirebaseUser user = fbAuth.getCurrentUser();
        if (user == null)
            Toast.makeText(this.getContext(), "user не авторизован", Toast.LENGTH_SHORT).show();
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

        fbAuth.signInWithEmailAndPassword(login, password)
                .addOnCompleteListener(this.getActivity(), (taskSignIn) -> {
                    if (taskSignIn.isSuccessful()) {
                        FirebaseUser user = fbAuth.getCurrentUser();
                        listener.authSuccessful(user);
                    } else {
                        Toast.makeText(this.getContext(), "Неудачный вход", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @OnClick(R.id.buttonRegister)
    protected void clickRegister() {
        String email = inputLogin.getText().toString();
        String password = inputPassword.getText().toString();
        Activity activity = Objects.requireNonNull(this.getActivity());
        fbAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, authTask -> {
                    if (authTask.isSuccessful()) {
                        Timber.i("Пользователь создан");
                        Toast.makeText(this.getContext(), "Пользователь создан", Toast.LENGTH_SHORT).show();
                    } else {
                        Timber.w("Не удалось создать польщователя");
                    }
                });
    }

    public interface Listener {
        void authSuccessful(FirebaseUser user);
    }
}
