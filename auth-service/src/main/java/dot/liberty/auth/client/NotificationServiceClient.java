package dot.liberty.auth.client;

import dot.liberty.auth.client.dto.SendEmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationServiceClient {

    @PostMapping("/api/notifications/send-email")
    void sendEmail(@RequestBody SendEmailRequest request);

}
