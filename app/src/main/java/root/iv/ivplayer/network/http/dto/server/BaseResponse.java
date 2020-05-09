package root.iv.ivplayer.network.http.dto.server;

import androidx.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {
    private Integer errorCode;
    private String errorMsg;
    @Nullable
    private T data;
}
