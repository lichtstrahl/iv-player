package root.iv.ivplayer.network.ws.dto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseMessageWS {
    @Nullable
    private String author;
    @Nullable
    private String toUser;
    @Nullable
    private String toRoom;
    @NonNull
    private TypeMSG type;
}
