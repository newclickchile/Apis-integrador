package cl.integrador.bsale.woowup.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@Slf4j
public class ProofyService {

    private static final String CALL_URL = "https://apis.proofy.io/v1";

    public static String validaEmailProofy(String email, String tokenApi) {
        log.info("[ GET CHECK MAIL FROM PROOFY ] [ Call for: {} ]", email);
        try {
            WebClient webClient;
            webClient = WebClient.builder()
                    .baseUrl(CALL_URL)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/verify/single")
                            .queryParam("api_key",tokenApi)
                            .queryParam("email", email)
                            .build())
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.info("Call successful for"))
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error Status code: {}. Response body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return e.getStatusCode().toString();
        } catch (WebClientException e) {
            log.error("Error for: {}, resultado {}", email, e.getMessage());
        }
        return HttpStatus.INTERNAL_SERVER_ERROR.toString();
    }

}
