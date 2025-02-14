package cl.integrador.bsale.woowup.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
@Service
@Slf4j
public class EmailService {

    private static final String VALIDATION_URL = "https://api.emailvalidator.com/check";
    private final RestTemplate restTemplate;

    public EmailService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Async("asyncTaskExecutor")
    public void validateEmailAsync(String email) {
        try {
            String response = restTemplate.getForObject(
                    VALIDATION_URL + "?email={email}",
                    String.class,
                    email
            );
            log.info("Validation successful for: {}. Response: {}", email, response);
        } catch (Exception e) {
            log.error("Error validating email: {}", email, e);
        }
    }
}