package root.iv.ivplayer.network.firebase.dto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
    protected String emailPlayer1;
    @Nullable
    protected String emailPlayer2;
    protected RoomState state;

    public int countPlayer() {
        int count = 0;

        count += (player1InRoom()) ? 1 : 0;
        count += (player2InRoom()) ? 1 : 0;

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
        return (email.equals(emailPlayer1))
                ? "emailPlayer1"
                : "emailPlayer2";
    }

    public String getEnemyProgressPath(String email) {
        BlockState currentState = getCurrentRole(email);
        return currentState == BlockState.CROSS
                ? PROGRESS_PATH_CIRCLE
                : PROGRESS_PATH_CROSS;
    }

    public boolean player1InRoom() {
        return emailPlayer1 != null && !emailPlayer1.isEmpty();
    }

    public boolean player2InRoom() {
        return emailPlayer2 != null && !emailPlayer2.isEmpty();
    }
}
