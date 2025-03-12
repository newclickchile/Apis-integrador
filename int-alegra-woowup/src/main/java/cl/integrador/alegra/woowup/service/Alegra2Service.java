package cl.integrador.alegra.woowup.service;

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
public class Alegra2Service {

    @Value("${url.alegra}")
    private String callUrl;

    private final WebClient webClient;

    public Alegra2Service() {
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String getInfoFactura(String idVenta, String token, int reintentos)   {
        boolean reintentar = false;
        try {
            log.info("[ GET INFO FROM ALEGRA ] [ Call for: {} ]", idVenta);
            log.info("[ GET INFO FROM ALEGRA ] [ Call for: {} ]", token);
            String url = callUrl + "/invoices/ " + idVenta ;
            log.info("[ VAR ] [ GET url: {} ]", url);
            return webClient.get()
                    .uri(url)
                    .header("access_token", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.info("Call successful for: {} ", idVenta))
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error for: {}. Status code: {}. Response body: {}",
                    idVenta, e.getStatusCode(), e.getResponseBodyAsString());
            reintentar = true;
        } catch (WebClientException e) {
            log.error("Error for {}: {}", idVenta, e.getMessage());
            reintentar = true;
        }
        if(reintentar && reintentos <= 3){
                log.error("REINTENTO {} for {} ", reintentos, idVenta );
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
                reintentos++;
                return getInfoFactura(idVenta, token, reintentos );
        }
        return null;
    }

}
