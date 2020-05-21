package root.iv.ivplayer.game.fanorona;

import android.view.MotionEvent;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import root.iv.ivplayer.game.room.Room;
import root.iv.ivplayer.game.room.RoomListener;
import root.iv.ivplayer.game.view.GameView;
import root.iv.ivplayer.network.firebase.dto.FBRoom;

public class FanoronaRoom extends Room {
    private FanoronaEngine engine;
    @Nullable
    private Listener roomListener;
    private FBRoom fbRoom;
    private FirebaseUser fbUser;
    private List<ValueEventListener> fbObservers;

    public FanoronaRoom(FanoronaTextures textures, String name, FirebaseUser user) {
        super(name);

        this.fbUser = user;
        fbObservers = new ArrayList<>();

        engine = new FanoronaEngine(textures, this::touchHandler);
    }

    @Override
    public void addListener(RoomListener listener) {

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void connect(GameView gameView) {

    }

    @Override
    public void exit() {

    }

    @Override
    public void init() {

    }

    private void touchHandler(MotionEvent event) {

    }

    public interface Listener extends RoomListener {
    }
}
