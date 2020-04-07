package root.iv.ivplayer.network.http.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDTO {
    private String login;
    private String password;
    private String firstName;
    private String lastName;
}

/**
 * data class UserCreateDTO(
 *         var login: String,
 *         var password: String,
 *         var firstName: String,
 *         var lastName: String
 * )
 */
