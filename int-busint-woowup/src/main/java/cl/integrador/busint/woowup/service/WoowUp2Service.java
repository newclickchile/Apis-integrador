package cl.integrador.busint.woowup.service;

import cl.integrador.busint.woowup.model.pojo.ClienteWoowup;
import cl.integrador.busint.woowup.util.WooeUpHelper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
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
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public HttpStatusCode existeCliente(ClienteWoowup cw, String token) {
        log.info("[ GET CLIENT FROM WoowUp ] [ Call for: {} ]", cw.getDocument());
        String url = CALL_URL + "/multiusers/find?document=" + cw.getDocument();
        log.info("[ VAR ] [ GET url: {} ]", url);
        try {
            return webClient.get()
                    .uri(url)
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
            String url = CALL_URL + "/multiusers";
            log.info("[ VAR ] [ PUT url: {} ]", url);
            webClient.put()
                    .uri(url)
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
            String url = CALL_URL + "/users" ;
            log.info("[ VAR ] [ POST url: {} ]", url);
            webClient.post()
                .uri(url)
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
            JsonObject jsonObject = JsonParser.parseString(e.getResponseBodyAsString()).getAsJsonObject();
            String messsage = jsonObject.get("messsage").getAsString();
            if(null!= messsage && messsage.contains("usuario existente")){
                log.debug("{ ATENCION } Status code: {}. Response body: {}", e.getStatusCode(), e.getResponseBodyAsString());
                return true;
            }
            log.error("Error Status code: {}. Response body: {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (WebClientException e) {
            log.error("Error for: {}", cw.getDocument(), e.getMessage());
        }
        log.info("[ CREATE Client ] Failed");
        return false;
    }

}
