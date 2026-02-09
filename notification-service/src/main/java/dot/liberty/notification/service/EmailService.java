package dot.liberty.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final JavaMailSender mailSender;

}
