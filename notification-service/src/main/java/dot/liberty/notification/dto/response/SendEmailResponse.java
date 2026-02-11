package dot.liberty.notification.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendEmailResponse {

    private String message;
    private String recipient;

    private LocalDateTime sentAt;

}
