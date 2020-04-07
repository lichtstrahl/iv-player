package root.iv.ivplayer.network.http.dto;

import java.util.Calendar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntityDTO {
    private Long id;
    private String login;
    private String password;
    private String firstName;
    private String lastName;
    private Calendar lastAccessTime;
    private String uuid;
}
/**
 data class UserEntityDTO(
 var id: Long,
 var login: String,
 var password: String,
 var firstName: String,
 var lastName: String,
 var lastAccessTime: Calendar,
 var uuid: String
 )
 **/