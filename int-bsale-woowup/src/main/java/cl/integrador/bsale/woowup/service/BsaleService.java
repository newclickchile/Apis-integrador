package cl.integrador.bsale.woowup.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Service
@Slf4j
public class BsaleService {

    private static final String CALL_URL = "https://api.bsale.io/v1";
    private final RestTemplate restTemplate;

    public BsaleService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .build();
    }

   // @Async("asyncTaskExecutor")
    public String getInfo(String json, String token) {
        try {
            log.info("[ GET INFO FROM BSALE ] [ Call for: {} ", json );
            log.info("[ GET INFO FROM BSALE ] [ Call for: {} ", token );
            HttpHeaders headers = new HttpHeaders();
            headers.set("access_token", token);
            //headers.set("Content-Type", "application/json"); // Puedes agregar más headers si es necesario

            // Crear la entidad HTTP con los headers
            HttpEntity<String> entity = new HttpEntity<>(headers);

//            String response = restTemplate.getForObject(
//                    CALL_URL + "/documents/{json}?expand=details,client,document_type,office,attributes,payments",
//                    String.class,
//                    json
//            );
            String response = restTemplate.exchange(
                    CALL_URL + "/documents/{json}?expand=details,client,document_type,office,attributes,payments",
                    HttpMethod.GET,
                    entity,
                    String.class,
                    json
            ).getBody();
            log.info("Call successful for: {}. Response: {}", json, response);
            return response;
        } catch (Exception e) {
            log.error("Error for: {}", json, e);
        }
        return null;
    }

    public String getProduct(String brand, String token) {
        try {
            log.info("[ GET PRODUCT FROM BSALE ] [ Call for: {} ", brand );
            log.info("[ GET PRODUCT FROM BSALE ] [ Call for: {} ", token );
            HttpHeaders headers = new HttpHeaders();
            headers.set("access_token", token);
            //headers.set("Content-Type", "application/json"); // Puedes agregar más headers si es necesario

            // Crear la entidad HTTP con los headers
            HttpEntity<String> entity = new HttpEntity<>(headers);

//            String response = restTemplate.getForObject(
//                    "{brand}?expand=costs,product,product_type",
//                    String.class,
//                    brand
//            );
            log.info("[ VAR ] [ url : {} ", brand + "?expand=costs,product,product_type" );
            String response = restTemplate.exchange(
                    brand + "?expand=costs,product,product_type",
                    HttpMethod.GET,
                    entity,
                    String.class,
                    brand
            ).getBody();

            response = StringEscapeUtils.unescapeHtml4(response);

            log.info("Call successful for: {}" , brand );
            return response;
        } catch (Exception e) {
            log.error("Error for: {}", brand, e);
        }
        return null;
    }
}
