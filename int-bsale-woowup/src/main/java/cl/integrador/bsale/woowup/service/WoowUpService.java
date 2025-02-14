package cl.integrador.bsale.woowup.service;

import cl.integrador.bsale.woowup.model.pojo.ClienteWoowup;
import cl.integrador.bsale.woowup.model.pojo.VentaWoowup;
import cl.integrador.bsale.woowup.util.WooeUpHelper;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Service
@Slf4j
public class WoowUpService {

    private static final String CALL_URL = "https://api.woowup.com/apiv3";
    private final RestTemplate restTemplate;

    public WoowUpService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .build();
    }

   // @Async("asyncTaskExecutor")
    public HttpStatusCode existeCliente(ClienteWoowup cw, String token) {
        try {
            log.info("[ GET CLIENT FROM WoowUp ] [ Call for: {} ", cw.getDocument() );
            log.info("[ GET CLIENT FROM WoowUp ] [ Call for: {} ", token );
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + token);
            headers.set("Content-Type", "application/json");
            headers.set("Accept", "application/json");
            // Crear la entidad HTTP con los headers
            HttpEntity<String> entity = new HttpEntity<>(headers);
//            String response = restTemplate.exchange(
//                    CALL_URL + "/multiusers/find?document={rut}",
//                    HttpMethod.GET,
//                    entity,
//                    String.class,
//                    cw.getDocument()
//            ).getBody();
            ResponseEntity<String> response = restTemplate.exchange(
                    CALL_URL + "/multiusers/find?document={rut}",
                    HttpMethod.GET,
                    entity,
                    String.class,
                    cw.getDocument()
            );

            log.info("Call successful code response : {}", response.getStatusCode());
            return response.getStatusCode();

        } catch (HttpClientErrorException e) {
            int statusCode = e.getStatusCode().value();
            log.error("Error  Status code: {}. Response body: {}", statusCode, e.getResponseBodyAsString());
            return HttpStatusCode.valueOf(statusCode);
        } catch (Exception e) {
            log.error("Error for: {}", cw.getDocument(), e);
        }
        log.info("[ Exist Client ] [ false ] "  );
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public boolean actualizaCliente(ClienteWoowup cw, String token) {
        try {

            log.info("[ UPDATE Client WoowUp ] [ Call for: {} ", token );
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + token);
            headers.set("Content-Type", "application/json");
            headers.set("Accept", "application/json");
            String jsonPut = WooeUpHelper.getJsonDatosCliente(cw);
            log.info("[ UPDATE Client WoowUp ] [ Call for: {} ", jsonPut);
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonPut, headers);
            RestTemplate rt = new RestTemplate();
            rt.put(CALL_URL + "/multiusers", requestEntity, String.class);
            log.info("[ UPDATE Client ] [ {} ] ", true );
            return true;
        } catch (Exception e) {
            log.error("Error for: {}", cw.getDocument(), e);
        }
        log.info("[ UPDATE Client ] [ {} ] ", false );
        return false;
    }

    public boolean creaCliente(ClienteWoowup cw, String token) {
        try {
            log.info("[ CREATE Client WoowUp ] [ Call for: {} ", token );
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + token);
            headers.set("Content-Type", "application/json");
            headers.set("Accept", "application/json");
            String jsonPut = WooeUpHelper.getJsonDatosCliente(cw);
            log.info("[ CREATE Client WoowUp ] [ Call for: {} ", jsonPut );
            HttpEntity<String> requestEntity = new HttpEntity<>(jsonPut, headers);
            RestTemplate rt = new RestTemplate();
            ResponseEntity<String> response = rt.postForEntity(CALL_URL + "/users", requestEntity, String.class);
            log.info("[ CREATE Client ] [ {} ] ", true );
            return true;
        } catch (Exception e) {
            log.error("Error for: {}", cw.getDocument(), e);
        }
        log.info("[ CREATE Client ] [ {} ] ", false );
        return false;
    }

    public boolean ingresarVenta(VentaWoowup v, String token) {
        try {
            log.info("[ CREATE Sale WoowUp ] [ Call for: {} ", v.getDocument() );
            log.info("[ CREATE Sale WoowUp ] [ Call for: {} ", token );
            Gson gson = new Gson();
            log.info("Object in JSON format: {}",   StringEscapeUtils.unescapeJava( gson.toJson(v) ));
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + token);
            //headers.set("cache-control", "no-cache");
           // headers.set("Accept", "application/json");
            HttpEntity<String> requestEntity = new HttpEntity<>( StringEscapeUtils.unescapeJava( gson.toJson(v) ), headers);
            RestTemplate rt = new RestTemplate();
            ResponseEntity<String> response = rt.postForEntity(CALL_URL + "/purchases", requestEntity, String.class);
            log.info("[ CREATE Sale ] [ {} ] ", true );
            return true;
        } catch (Exception e) {
            log.error("Error for: {}", v.getDocument(), e);
        }
        log.info("[ CREATE Sale ] [ {} ] ", false );
        return false;
    }



}
