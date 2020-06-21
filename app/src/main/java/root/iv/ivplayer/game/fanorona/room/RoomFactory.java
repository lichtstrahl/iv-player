package root.iv.ivplayer.game.fanorona.room;

import com.google.firebase.auth.FirebaseUser;

import root.iv.ivplayer.game.fanorona.FanoronaTextures;

public class RoomFactory {

    public static class Fanorona {
        public static FanoronaRoom multiplayer(FanoronaTextures textures, String name, FirebaseUser user) {
            return new FanoronaRoom(textures, name, user);
        }

        public static FanoronaLocalRoom local(FanoronaTextures textures) {
            return new FanoronaLocalRoom(textures);
        }
    }


}
