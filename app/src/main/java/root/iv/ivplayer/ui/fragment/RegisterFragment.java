package root.iv.ivplayer.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.CompositeDisposable;
import root.iv.ivplayer.R;
import root.iv.ivplayer.network.http.dto.server.UserEntityDTO;

public class RegisterFragment extends Fragment {
    @BindView(R.id.inputFirstName)
    protected EditText inputFirstName;
    @BindView(R.id.inputLastName)
    protected EditText inputLastName;
    @BindView(R.id.inputLogin)
    protected EditText inputLogin;
    @BindView(R.id.inputPassword)
    protected EditText inputPassword;

    private CompositeDisposable disposable;
    private Listener listener;

    public static RegisterFragment getInstance() {
        return new RegisterFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reg, container, false);
        ButterKnife.bind(this, view);

        disposable = new CompositeDisposable();

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof Listener)
            listener = (Listener) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        disposable.dispose();
        listener = null;
    }

    @OnClick(R.id.buttonRegister)
    public void clickRegister() {
        String password = inputPassword.getText().toString();
        String encodePassword = Base64.encodeToString(password.getBytes(), Base64.DEFAULT);


//        UserCreateDTO createDTO = UserCreateDTO.builder()
//                .firstName(inputFirstName.getText().toString())
//                .lastName(inputLastName.getText().toString())
//                .login(inputLogin.getText().toString())
//                .password(encodePassword)
//                .build();

//        Disposable d = App.getPlayerAPI().register(createDTO)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(
//                        listener::registerSuccessful,
//                        App::logE
//                );
//        disposable.add(d);
    }

    public interface Listener {
        void registerSuccessful(UserEntityDTO dto);
    }
}
