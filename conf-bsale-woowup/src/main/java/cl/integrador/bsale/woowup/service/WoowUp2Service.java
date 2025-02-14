package cl.integrador.bsale.woowup.service;

import cl.integrador.bsale.woowup.model.pojo.ClienteWoowup;
import cl.integrador.bsale.woowup.model.pojo.VentaWoowup;
import cl.integrador.bsale.woowup.model.pojo.WoowupResponse;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@Slf4j
public class WoowUp2Service {

    private static final String CALL_URL = "https://api.woowup.com/apiv3";
    private static final Gson GSON = new Gson();

    private final WebClient webClient;

    public WoowUp2Service() {
        this.webClient = WebClient.builder()
                .baseUrl(CALL_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public HttpStatusCode validaKeyWoowup( String token) {
        log.info("[ GET CHECK TOKEN FROM WOOWUP ] [ Call for: {} ]", token);
        try {
            webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/multiusers/exist")
                            .queryParam("email", "test@email.com")
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + token)
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .retrieve()
                    .toBodilessEntity()
                    .retryWhen(Retry.fixedDelay(2, Duration.ofSeconds(2))
                            .filter(throwable -> throwable instanceof WebClientResponseException &&
                                    ((WebClientResponseException) throwable).getStatusCode() == HttpStatus.TOO_MANY_REQUESTS))
                    .block()
                    .getStatusCode();
                     return HttpStatus.OK;
        } catch (WebClientResponseException e) {
            log.error("Error Status code: {}. Response body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return e.getStatusCode();
        } catch (WebClientException e) {
            log.error("Error for: {}", token, e.getMessage());
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

}
