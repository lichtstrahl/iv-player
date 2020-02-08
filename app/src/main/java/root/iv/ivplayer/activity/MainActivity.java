package root.iv.ivplayer.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import io.reactivex.disposables.CompositeDisposable;
import root.iv.ivplayer.R;
import root.iv.ivplayer.notification.NotificationPublisher;
import root.iv.ivplayer.ws.EchoWSListener;
import root.iv.ivplayer.ws.WSHolder;
import root.iv.ivplayer.ws.WSUtil;

public class MainActivity extends AppCompatActivity {
    private static final String SAVE_VIEW = "save:view";
    private static final String SAVE_INPUT = "save:input";

    private Button button;
    private TextView view;
    private WSHolder wsHolder;
    private EditText input;
    private CompositeDisposable disposable;
    private NotificationPublisher notificationPublisher;
    private boolean showPushNotify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = this.findViewById(R.id.button);
        view = this.findViewById(R.id.view);
        input = this.findViewById(R.id.input);

        EchoWSListener listener = new EchoWSListener();
        wsHolder = new WSHolder(WSUtil.templateURL(), listener);
        disposable = new CompositeDisposable();

        if (savedInstanceState != null)
            reload(savedInstanceState);


        notificationPublisher = new NotificationPublisher(getApplicationContext());

        button.setOnClickListener(this::click);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVE_VIEW, view.getText().toString());
        outState.putString(SAVE_INPUT, input.getText().toString());
    }

    @Override
    protected void onStart() {
        super.onStart();
        wsHolder.open(this::recv);
        showPushNotify = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        wsHolder.close();
        disposable.dispose();
        showPushNotify = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("tag:ws", "DESTROY");
    }

    private void click(View view) {
        String text = input.getText().toString();
        if (!text.isEmpty()) {
            wsHolder.send(text);
            appendMsg("Я: " + text);
            input.getText().clear();
        }
    }

    private void recv(String msg) {
        runOnUiThread(() -> appendMsg("Некто: " + msg));
        if (showPushNotify) notificationPublisher.notification(msg);
    }

    private void appendMsg(String msg) {
        String str = view.getText().toString();
        str = str.concat(msg).concat("\n");
        view.setText(str);
    }

    private void reload(@NonNull Bundle bundle) {
        String saveMsg = bundle.getString(SAVE_VIEW);
        view.setText(saveMsg);
        String saveInput = bundle.getString(SAVE_INPUT);
        input.setText(saveInput);
    }
}
