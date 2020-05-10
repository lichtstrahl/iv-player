package root.iv.ivplayer.game.tictac;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import root.iv.ivplayer.game.room.RoomState;
import root.iv.ivplayer.game.tictac.dto.TicTacDTO;
import root.iv.ivplayer.game.tictac.dto.TicTacDTOType;
import root.iv.ivplayer.game.tictac.dto.TicTacProgressDTO;
import root.iv.ivplayer.game.tictac.dto.TicTacRoomStatusDTO;
import root.iv.ivplayer.game.tictac.dto.TicTacEndDTO;
import root.iv.ivplayer.network.ws.dto.BaseMessageWS;
import root.iv.ivplayer.network.ws.dto.PlayerLifecycleMSG;


public class TicTacJsonProcessor {
    private GsonBuilder gsonBuilder;

    public TicTacJsonProcessor() {
        this.gsonBuilder = new GsonBuilder();
    }

    public TicTacDTOType dtoType(String json) {
        Type type = new TypeToken<TicTacDTO>(){}.getType();
        TicTacDTO dto = gsonBuilder.create().fromJson(json, type);
        return dto.getType();
    }

    public TicTacProgressDTO receiveProgressDTO(String json) {
        Type type = new TypeToken<TicTacDTO<TicTacProgressDTO>>(){}.getType();
        TicTacDTO<TicTacProgressDTO> progress = gsonBuilder.create().fromJson(json, type);
        return progress.getData();
    }

    public TicTacEndDTO receiveWinDTO(String json) {
        Type type = new TypeToken<TicTacDTO<TicTacEndDTO>>(){}.getType();
        TicTacDTO<TicTacEndDTO> win = gsonBuilder.create().fromJson(json, type);
        return win.getData();
    }

    public TicTacRoomStatusDTO reciveStatusRoomDTO(String json) {
        Type type = new TypeToken<TicTacDTO<TicTacRoomStatusDTO>>(){}.getType();
        TicTacDTO<TicTacRoomStatusDTO> roomStatus = gsonBuilder.create().fromJson(json, type);
        return roomStatus.getData();
    }


    public BaseMessageWS receiveBase(String json) {
        return gsonBuilder.create().fromJson(json, BaseMessageWS.class);
    }

    public <T> T receive(String json, Class<T> cls) {
        return gsonBuilder.create().fromJson(json, cls);
    }

    public String buildWinDTO(String uuid) {
        TicTacDTO<TicTacEndDTO> winMessage = new TicTacDTO<>(
                TicTacDTOType.END, new TicTacEndDTO(uuid, true)
        );
        return  toJson(winMessage);
    }

    public String buildEndDTO(String uuid) {
        TicTacDTO<TicTacEndDTO> endMessage = new TicTacDTO<>(
                TicTacDTOType.END, new TicTacEndDTO(uuid, false)
        );

        return toJson(endMessage);
    }

    public String buildRoomStatusDTO(String uuid, RoomState roomState) {
        TicTacDTO<TicTacRoomStatusDTO> roomStatusMsg = new TicTacDTO<>(
                TicTacDTOType.ROOM_STATE, new TicTacRoomStatusDTO(uuid, roomState)
        );
        return toJson(roomStatusMsg);
    }

    public String buildProgressDTO(TicTacProgressDTO progress) {

        TicTacDTO<TicTacProgressDTO> progressMsg = new TicTacDTO<>(
                TicTacDTOType.PROGRESS, progress
        );
        return toJson(progressMsg);
    }

    public String toJson(Object object) {
        return gsonBuilder.create().toJson(object);
    }
}
