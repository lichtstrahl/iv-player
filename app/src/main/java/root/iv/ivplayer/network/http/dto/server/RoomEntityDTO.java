package root.iv.ivplayer.network.http.dto.server;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomEntityDTO {
    private Long id;
    private String name;
    private long createDate; // milliseconds
    private String host;
    private boolean live;
}
