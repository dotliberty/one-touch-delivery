package dot.liberty.notification.controller;

import dot.liberty.notification.dto.request.SendEmailRequest;
import dot.liberty.notification.dto.response.SendEmailResponse;
import dot.liberty.notification.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final EmailService emailService;

    @PostMapping("/send-email")
    public ResponseEntity<SendEmailResponse> sendEmail(@Valid @RequestBody SendEmailRequest request) {
        log.info("Received request to send email to: {}", request.getTo());

        emailService.sendEmail(
                request.getTo(),
                request.getSubject(),
                request.getBody()
        );

        SendEmailResponse response = SendEmailResponse.builder()
                .message("Email sent successfully")
                .recipient(request.getTo())
                .sentAt(LocalDateTime.now())
                .build();

        log.info("Email sent successfully to: {}", request.getTo());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Object> health() {
        return ResponseEntity.ok(java.util.Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now()
        ));
    }

}
