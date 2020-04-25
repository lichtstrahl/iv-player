package root.iv.ivplayer.game.tictac;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import root.iv.ivplayer.network.ws.pubnub.dto.TicTacDTO;
import root.iv.ivplayer.network.ws.pubnub.dto.TicTacDTOType;
import root.iv.ivplayer.network.ws.pubnub.dto.TicTacProgressDTO;
import root.iv.ivplayer.network.ws.pubnub.dto.TicTacWinDTO;


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

    public TicTacWinDTO receiveWinDTO(String json) {
        Type type = new TypeToken<TicTacDTO<TicTacWinDTO>>(){}.getType();
        TicTacDTO<TicTacWinDTO> win = gsonBuilder.create().fromJson(json, type);
        return win.getData();
    }

    public String buildWinDTO(String uuid) {
        TicTacDTO<TicTacWinDTO> winMessage = new TicTacDTO<>(
                TicTacDTOType.WIN, new TicTacWinDTO(uuid, true)
        );
        return  toJson(winMessage);
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
