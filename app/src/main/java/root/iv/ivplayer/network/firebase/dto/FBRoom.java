package root.iv.ivplayer.network.firebase.dto;

import androidx.annotation.NonNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import root.iv.ivplayer.game.room.RoomState;
import root.iv.ivplayer.game.tictac.BlockState;
import root.iv.ivplayer.game.tictac.TicTacEngine;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FBRoom {
    private String emailPlayer1;
    private String emailPlayer2;
    private RoomState state;

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

    public String getProgressPath(String email) {
        BlockState currentState = getCurrentRole(email);
        return currentState == BlockState.CROSS
                ? "progressCROSS"
                : "progressCIRCLE";

    }
}
