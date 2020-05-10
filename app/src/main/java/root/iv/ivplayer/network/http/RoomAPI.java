package root.iv.ivplayer.network.http;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;
import root.iv.ivplayer.network.http.dto.server.BaseResponse;
import root.iv.ivplayer.network.http.dto.server.RoomEntityDTO;

public interface RoomAPI {
    @GET("all")
    Single<List<RoomEntityDTO>> getAllRooms();
    @GET("join")
    Single<BaseResponse<Void>> joinPlayer(@Query("login") String login, @Query("room") String room);
    @GET("leave")
    Single<BaseResponse<Void>> leavePlayer(@Query("login") String login, @Query("room") String room);
}
