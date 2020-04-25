package root.iv.ivplayer.game.room;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum  RoomStateJump {
    NEW(RoomState.WAIT_PLAYERS),
    WAIT_PLAYERS(RoomState.GAME),
    GAME(RoomState.PAUSE, RoomState.CLOSE, RoomState.WAIT_PROGRESS),
    WAIT_PROGRESS(RoomState.GAME, RoomState.CLOSE),
    PAUSE(RoomState.GAME, RoomState.CLOSE),
    CLOSE();

    private List<RoomState> nextStates;

    RoomStateJump(RoomState ... states) {
        this.nextStates = Arrays.stream(states).collect(Collectors.toList());
    }

    // Может ли данное состояние измениться на заданное:
    public boolean possibleTransit(RoomState newState) {
        return nextStates.contains(newState);
    }

    public static RoomStateJump of(RoomState state) {
        return RoomStateJump.valueOf(state.name());
    }
}
