package root.iv.ivplayer.network.http;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.GET;
import root.iv.ivplayer.network.http.dto.server.RoomEntityDTO;

public interface RoomAPI {
    @GET("all")
    Single<List<RoomEntityDTO>> getAllRooms();
}
