package root.iv.ivplayer.network.firebase.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FBUser {
    private String login;
    private String password;
}
