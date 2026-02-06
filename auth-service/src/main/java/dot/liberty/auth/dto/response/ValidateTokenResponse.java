package dot.liberty.auth.dto.response;

import dot.liberty.auth.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidateTokenResponse {

    private Long userId;
    private String email;
    private Role role;
}
