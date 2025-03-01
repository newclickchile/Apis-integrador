package cl.integrador.bsale.woowup.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
@Service
@Slf4j
public class EmailService {

    @Value("${url.check.mail}")
    private String callUrl;

    private final WebClient webClient;

    public EmailService() {
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String getCheckEmailInfo(String cliente, String token, String correo) {
        try {
            log.info("[ GET INFO FROM BSALE ] [ Call for: {} - {} ]", cliente, correo);
            String url = callUrl + "/check?email="+correo;
            log.info("[ VAR ] [ GET url: {} ]", url);

            return webClient.get()
                    .uri(url)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .header("Cliente", cliente)
                    .header("Access_key", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.info("Call successful for: {} ", correo))
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error for: {}. Status code: {}. Response body: {}", correo, e.getStatusCode(), e.getResponseBodyAsString());
        } catch (WebClientException e) {
            log.error("Error for {}: {}", correo, e.getMessage());
        }
        return null;
    }

}