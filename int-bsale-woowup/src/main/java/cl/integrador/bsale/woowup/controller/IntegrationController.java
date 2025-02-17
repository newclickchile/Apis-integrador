package cl.integrador.bsale.woowup.controller;

import cl.integrador.bsale.woowup.model.entity.Cliente;
import cl.integrador.bsale.woowup.model.pojo.Senal;
import cl.integrador.bsale.woowup.repository.UserDataRepository;
import cl.integrador.bsale.woowup.repository.UserSucursalDataRepository;
import cl.integrador.bsale.woowup.service.AsyncService;
import cl.integrador.bsale.woowup.service.EmailService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
@RequestMapping("/v1")
@Slf4j
public class IntegrationController {

    static final String RESPUESTA_UNAUTHORIZED = "{ \"code\": 401, \"msg\": \"401 UNAUTHORIZED\" }";

    @Autowired
    private UserDataRepository userDataRepository;

    @Autowired
    private UserSucursalDataRepository userSucursalDataRepository;

    @Autowired
    private AsyncService asyncService;

    public ResponseEntity<Void> webhook(@RequestBody Senal senal,
                                        @RequestHeader("Cliente") String idCliente,
                                        @RequestHeader("Access_key") String accessKey) throws SQLException {
        log.info("[ ==================================== ]");
        log.info("[ =   S T A R T      W E B H O O K   = ]");
        log.info("[ ==================================== ]");
        log.debug("[ WEBHOOK ] Received request for process: {}", new Gson().toJson(senal));
        if(null == idCliente || null == accessKey|| null == senal){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Cliente u = userDataRepository.findByClientAndAccess(idCliente, accessKey);
        if(null == u){
            log.warn("Se ignora la informacion por que cliente/acceso no corresponden : {}", idCliente);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            log.debug("[ VAR ] Valida topico == 'document' : {} ",  senal.getTopic().equalsIgnoreCase("document"));
            if (senal.getTopic().equalsIgnoreCase("document")) {
                asyncService.procesoAsyncDelEvento(senal, u, idCliente, accessKey);
            } else {
                log.warn("Se ignora la informacion por que es : {}", senal.getTopic());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            log.error("[ ERROR ] [ GENERAL] [ RECEPCION DE EVENTO ][ WEBHOOK ]  {}", senal.getTopic());
        }
        log.debug("[ WEBHOOK ] Request procesed: {}", new Gson().toJson(senal));
        log.info("[ ================================ ]");
        log.info("[ =   E N D      W E B H O O K   = ]");
        log.info("[ ================================ ]");
        return ResponseEntity.status(HttpStatus.OK).build();
    }



}