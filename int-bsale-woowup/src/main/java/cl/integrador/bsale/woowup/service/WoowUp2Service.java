package cl.integrador.bsale.woowup.service;

import cl.integrador.bsale.woowup.model.pojo.ClienteWoowup;
import cl.integrador.bsale.woowup.model.pojo.VentaWoowup;
import cl.integrador.bsale.woowup.model.pojo.WoowupResponse;
import cl.integrador.bsale.woowup.util.WooeUpHelper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${url.woowup}")
    private String CALL_URL;

    private static final Gson GSON = new Gson();

    private final WebClient webClient;

    public WoowUp2Service() {
        this.webClient = WebClient.builder()
                .baseUrl(CALL_URL)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public HttpStatusCode existeCliente(ClienteWoowup cw, String token) {
        log.info("[ GET CLIENT FROM WoowUp ] [ Call for: {} ]", cw.getDocument());

        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/multiusers/find")
                            .queryParam("document", cw.getDocument())
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

        } catch (WebClientResponseException e) {
            log.error("Error Status code: {}. Response body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return e.getStatusCode();
        } catch (WebClientException e) {
            log.error("Error for: {}", cw.getDocument(), e.getMessage());
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public boolean actualizaCliente(ClienteWoowup cw, String token) {
        try {
            log.info("[ UPDATE Client WoowUp ] [ Call for: {} ]", token);
            String jsonPut = WooeUpHelper.getJsonDatosCliente(cw);
            log.info("[ UPDATE Client WoowUp ] [ JSON: {} ]", jsonPut);

            webClient.put()
                    .uri("/multiusers")
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + token)
                    .bodyValue(jsonPut)
                    .retrieve()
                    .bodyToMono(String.class)
                    .retryWhen(Retry.fixedDelay(2, Duration.ofSeconds(2))
                            .filter(throwable -> throwable instanceof WebClientResponseException &&
                                    ((WebClientResponseException) throwable).getStatusCode() == HttpStatus.TOO_MANY_REQUESTS))
                    .block();

            log.info("[ UPDATE Client ] Successful");
            return true;
        } catch (WebClientResponseException e) {
            log.error("Error Status code: {}. Response body: {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (WebClientException e) {
            log.error("Error for: {}", cw.getDocument(), e.getMessage());
        }
        log.info("[ UPDATE Client ] Failed");
        return false;
    }

    public boolean creaCliente(ClienteWoowup cw, String token) {
        try {
            log.info("[ CREATE Client WoowUp ] [ Call for: {} ]", token);
            String jsonPut = WooeUpHelper.getJsonDatosCliente(cw);
            log.info("[ CREATE Client WoowUp ] [ JSON: {} ]", jsonPut);

            webClient.post()
                .uri("/users")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + token)
                .bodyValue(jsonPut)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.fixedDelay(2, Duration.ofSeconds(2))
                        .filter(throwable -> throwable instanceof WebClientResponseException &&
                                ((WebClientResponseException) throwable).getStatusCode() == HttpStatus.TOO_MANY_REQUESTS))

                .block();

            log.info("[ CREATE Client ] Successful");
            return true;
        } catch (WebClientResponseException e) {
            log.error("Error Status code: {}. Response body: {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (WebClientException e) {
            log.error("Error for: {}", cw.getDocument(), e.getMessage());
        }
        log.info("[ CREATE Client ] Failed");
        return false;
    }

    public WoowupResponse ingresarVenta(VentaWoowup v, String token) {
        try {
            log.info("[ CREATE Sale WoowUp ] [ Call for: {} ]", v.getDocument());
            String jsonBody = StringEscapeUtils.unescapeJava(GSON.toJson(v));
            log.info("[ JSON to send: {} ]", jsonBody);

            webClient.post()
                .uri("/purchases")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + token)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .header(HttpHeaders.ACCEPT, "application/json")
                .bodyValue(jsonBody)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(Retry.fixedDelay(2, Duration.ofSeconds(2))
                        .filter(throwable -> throwable instanceof WebClientResponseException &&
                                ((WebClientResponseException) throwable).getStatusCode() == HttpStatus.TOO_MANY_REQUESTS))

                .block();

            log.info("[ CREATE Sale ] Successful");
            return new WoowupResponse(HttpStatus.OK.value(), "");
        } catch (WebClientResponseException e) {
            log.error("Error for: {}. Status code: {}. Response body: {}", v.getDocument(), e.getStatusCode(), e.getResponseBodyAsString());
            return new WoowupResponse(e.getStatusCode().value(),  e.getResponseBodyAsString());
        } catch (WebClientException e) {
            log.error("Error for: {}", v.getDocument(), e.getMessage());
        }
        log.info("[ CREATE Sale ] Failed");
        return new WoowupResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "ERROR AL INTENTAR INGRESAR LA VENTA");
    }
}
