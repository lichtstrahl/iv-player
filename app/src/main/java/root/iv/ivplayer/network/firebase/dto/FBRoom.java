package root.iv.ivplayer.network.firebase.dto;

import androidx.annotation.NonNull;

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

    protected String emailPlayer1;
    protected String emailPlayer2;
    protected RoomState state;

    public int countPlayer() {
        int count = 0;

        count += (emailPlayer1.isEmpty()) ? 0 : 1;
        count += (emailPlayer2.isEmpty()) ? 0 : 1;

        return count;
    }

    public boolean isJoinPlayer(@NonNull FBRoom oldRoom) {
        return countPlayer() > oldRoom.countPlayer();
    }

    public boolean isLeavePlayer(@NonNull FBRoom oldRoom) {
        return countPlayer() < oldRoom.countPlayer();
    }

    public boolean isChangeState(@NonNull FBRoom oldRoom) {
        return this.state != oldRoom.state;
    }

    public BlockState getCurrentRole(String email) {
        return (email.equals(emailPlayer1))
                ? BlockState.CROSS
                : BlockState.CIRCLE;
    }

    public String getCurrentProgressPath(String email) {
        BlockState currentState = getCurrentRole(email);
        return currentState == BlockState.CROSS
                ? PROGRESS_PATH_CROSS
                : PROGRESS_PATH_CIRCLE;
    }

    public String getCurrentEmailPath(String email) {
        return (emailPlayer1.equals(email))
                ? "emailPlayer1"
                : "emailPlayer2";
    }

    public String getEnemyProgressPath(String email) {
        BlockState currentState = getCurrentRole(email);
        return currentState == BlockState.CROSS
                ? PROGRESS_PATH_CIRCLE
                : PROGRESS_PATH_CROSS;
    }
}
