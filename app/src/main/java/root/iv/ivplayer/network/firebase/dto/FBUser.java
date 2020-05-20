package root.iv.ivplayer.network.firebase.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FBUser {
    private String name;
    private String uid;

    public static FBUser create(String name, String uid) {
        return new FBUser(name, uid);
    }
}
