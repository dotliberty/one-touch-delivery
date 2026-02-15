package dot.liberty.auth.service;

import dot.liberty.auth.client.NotificationServiceClient;
import dot.liberty.auth.client.dto.SendEmailRequest;
import dot.liberty.auth.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final NotificationServiceClient notificationServiceClient;

    public void sendVerificationCode(User user, String code) {
        String email = user.getEmail();

        String messageTextTemplate = """
                Your verification code is: %s
                
                This code expires in 15 minutes.""";

        String subject = "Verify your email - One Touch Delivery";
        String body = String.format(messageTextTemplate, code);

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .to(email)
                .subject(subject)
                .body(body)
                .build();

        notificationServiceClient.sendEmail(emailRequest);
    }

}
