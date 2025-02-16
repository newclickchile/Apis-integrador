package cl.integrador.bsale.woowup.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@Slf4j
public class Bsale2Service {

    private static final String CALL_URL = "https://api.bsale.io/v1";
    private final WebClient webClient;

    public Bsale2Service() {
        this.webClient = WebClient.builder()
                .baseUrl(CALL_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public HttpStatusCode validaKeyBsale(String token) {
        try {
            log.info("[ GET CHECK TOKEN FROM BSALE ] [ Call for: {} ]", token);
            String url = CALL_URL + "/users.json";
            log.info("[ VAR ] [ url: {} ]", url);
            webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/users.json")
                           .build())
                    .header("access_token", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.info("Call successful for: {} ", token))
                    .block();
            return HttpStatus.OK;
        } catch (WebClientResponseException e) {
            log.error("Error for: {}. Status code: {}. Response body: {}",
                    token, e.getStatusCode(), e.getResponseBodyAsString());
        } catch (WebClientException e) {
            log.error("Error for {}: {}", token, e.getMessage());
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
