package root.iv.ivplayer.network.http;

import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;
import root.iv.ivplayer.network.http.dto.UserCreateDTO;
import root.iv.ivplayer.network.http.dto.UserEntityDTO;

public interface IvPlayerAPI {
    @POST("/api/users/create")
    Single<UserEntityDTO> register(@Body UserCreateDTO createDTO);
}
