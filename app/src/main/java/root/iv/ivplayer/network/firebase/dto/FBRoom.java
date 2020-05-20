package root.iv.ivplayer.network.firebase.dto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.encoders.annotations.Encodable;

import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import root.iv.ivplayer.game.room.RoomState;
import root.iv.ivplayer.game.tictac.BlockState;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FBRoom {
    public static final String PROGRESS_PATH_CIRCLE = "progressCIRCLE";
    public static final String PROGRESS_PATH_CROSS = "progressCROSS";

    @Nullable
    protected FBUser player1;
    @Nullable
    protected FBUser player2;
    protected RoomState state;

    public int countPlayer() {
        int count = 0;

        count += (player1 != null) ? 1 : 0;
        count += (player2 != null) ? 1 : 0;

        return count;
    }

    public boolean isJoinPlayer(@NonNull FBRoom oldRoom) {
        return countPlayer() > oldRoom.countPlayer();
    }

    public boolean isLeavePlayer(@NonNull FBRoom oldRoom) {
        return countPlayer() < oldRoom.countPlayer();
    }

    public BlockState getCurrentRole(String uid) {
        return (uid.equals(player1.getUid()))
                ? BlockState.CROSS
                : BlockState.CIRCLE;
    }

    public String getCurrentProgressPath(String uid) {
        BlockState currentState = getCurrentRole(uid);
        return currentState == BlockState.CROSS
                ? PROGRESS_PATH_CROSS
                : PROGRESS_PATH_CIRCLE;
    }

    public String getCurrentPlayerPath(String uid) {
        return (player1 != null && uid.equals(player1.getUid()))
                ? "player1"
                : "player2";
    }

    public String getEnemyProgressPath(String uid) {
        BlockState currentState = getCurrentRole(uid);
        return currentState == BlockState.CROSS
                ? PROGRESS_PATH_CIRCLE
                : PROGRESS_PATH_CROSS;
    }

    public String name1() {
        if (player1 == null)
            return "";

        return (player1.getName() != null && !player1.getName().isEmpty())
                ? player1.getName()
                : player1.getUid();
    }

    public String name2() {
        if (player2 == null)
            return "";

        return (player2.getName() != null && !player2.getName().isEmpty())
                ? player2.getName()
                : player2.getUid();
    }

    // Номер игрока по id
    public int numberPlayer(String uid) {
        return (player1 != null && player1.getUid().equals(uid))
                ? 1
                : 2;
    }
}
