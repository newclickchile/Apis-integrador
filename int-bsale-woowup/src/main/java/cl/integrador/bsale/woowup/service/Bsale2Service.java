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
public class Bsale2Service {

    @Value("${url.bsale}")
    private String callUrl;

    private final WebClient webClient;

    public Bsale2Service() {
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String getInfo(String jsonName, String token, int reintentos)   {
        boolean reintentar = false;
        try {
            log.info("[ GET INFO FROM BSALE ] [ Call for: {} ]", jsonName);
            log.info("[ GET INFO FROM BSALE ] [ Call for: {} ]", token);
            String url = callUrl + "/documents/" + jsonName + "?expand=details,client,document_type,office,attributes,payments,sellers";
            log.info("[ VAR ] [ GET url: {} ]", url);
            return webClient.get()
                    .uri(url)
                    .header("access_token", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.info("Call successful for: {} ", jsonName))
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error for: {}. Status code: {}. Response body: {}", jsonName, e.getStatusCode(), e.getResponseBodyAsString());
            reintentar = true;
        } catch (WebClientException e) {
            log.error("Error for {}: {}", jsonName, e.getMessage());
            reintentar = true;
        }
        if(reintentar && reintentos <= 3){
                log.error("REINTENTO {} for {} ", reintentos, jsonName );
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                reintentos++;
                return getInfo(jsonName, token, reintentos );
        }
        return null;
    }
    public String getProduct(String brand, String token) {
        try {
            log.info("[ GET PRODUCT FROM BSALE ] [ Call for: {} ", brand);
            log.info("[ GET PRODUCT FROM BSALE ] [ Call for: {} ", token);
            String url = brand + "?expand=costs,product,product_type";
            log.info("[ VAR ] [ GET url : {} ", url);
            return  webClient.get()
                    .uri(url)
                    .header("access_token", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(res -> log.info("Call successful for: {} ", brand))
                    .block(); // Bloquea para obtener el resultado (no reactivo en este caso)
        } catch (WebClientResponseException e) {
            log.error("Error for: {}. Status code: {}. Response body: {}",
                    brand, e.getStatusCode(), e.getResponseBodyAsString() );
        } catch (Exception e) {
            log.error("Error for {}: {}", brand, e.getMessage());
        }
        return null;
    }
}