package cl.integrador.bsale.woowup.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.http.HttpHeaders;
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

    public String getInfo(String jsonName, String token) {
        try {
            log.info("[ GET INFO FROM BSALE ] [ Call for: {} ]", jsonName);
            log.info("[ GET INFO FROM BSALE ] [ Call for: {} ]", token);
            String url = CALL_URL + "/documents/" + jsonName + "?expand=details,client,document_type,office,attributes,payments,sellers";
            log.info("[ VAR ] [ url: {} ]", url);
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/documents/{jsonName}")
                            .queryParam("expand", "details,client,document_type,office,attributes,payments,sellers")
                            .build(jsonName))
                    .header("access_token", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSuccess(response -> log.info("Call successful for: {} ", jsonName))
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Error for: {}. Status code: {}. Response body: {}", jsonName, e.getStatusCode(), e.getResponseBodyAsString());
        } catch (WebClientException e) {
            log.error("Error for {}: {}", jsonName, e.getMessage());
        }
        return null;
    }
    public String getProduct(String brand, String token) {
        try {
            log.info("[ GET PRODUCT FROM BSALE ] [ Call for: {} ", brand);
            log.info("[ GET PRODUCT FROM BSALE ] [ Call for: {} ", token);
            String url = brand + "?expand=costs,product,product_type";
            log.info("[ VAR ] [ url : {} ", url);
            return  webClient.get()
                    .uri(url)
                    .header("access_token", token)
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(StringEscapeUtils::unescapeHtml4) // Escapa el HTML
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
