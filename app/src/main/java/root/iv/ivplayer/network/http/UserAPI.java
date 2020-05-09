package root.iv.ivplayer.network.http;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;
import root.iv.ivplayer.network.http.dto.server.AuthResponse;
import root.iv.ivplayer.network.http.dto.server.BaseResponse;

public interface UserAPI {
    @GET("auth")
    Single<BaseResponse<AuthResponse>> auth(@Query("login") String login, @Query("password") String password);
}
