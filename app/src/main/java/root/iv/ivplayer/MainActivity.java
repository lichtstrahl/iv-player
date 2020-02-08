package root.iv.ivplayer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import io.reactivex.disposables.CompositeDisposable;
import root.iv.ivplayer.ws.EchoWSListener;
import root.iv.ivplayer.ws.WSHolder;
import root.iv.ivplayer.ws.WSUtil;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private TextView view;
    private WSHolder wsHolder;
    private EditText input;
    private CompositeDisposable disposable;

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


        button.setOnClickListener(this::click);
    }

    @Override
    protected void onStart() {
        super.onStart();
        wsHolder.open(this::recv);
    }

    @Override
    protected void onStop() {
        super.onStop();
        wsHolder.close();
        disposable.dispose();
    }

    private void click(View view) {
        String text = input.getText().toString();
        if (!text.isEmpty()) {
            wsHolder.send(text);
            appendMsg("Я: " + text);
        }
    }

    private void recv(String msg) {
        runOnUiThread(() -> appendMsg("Некто: " + msg));
    }

    private void appendMsg(String msg) {
        String str = view.getText().toString();
        str = str.concat(msg).concat("\n");
        view.setText(str);
    }
}
