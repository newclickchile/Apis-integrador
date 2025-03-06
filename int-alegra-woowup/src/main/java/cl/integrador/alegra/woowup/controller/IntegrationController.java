package cl.integrador.alegra.woowup.controller;

import cl.integrador.alegra.woowup.model.entity.Cliente;
import cl.integrador.alegra.woowup.repository.UserDataRepository;
import cl.integrador.alegra.woowup.repository.UserSucursalDataRepository;
import cl.integrador.alegra.woowup.service.ClientAsyncService;
import cl.integrador.alegra.woowup.service.SaleAsyncService;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.Base64;

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
    private SaleAsyncService saleAsyncService;

    @Autowired
    private ClientAsyncService clientAsyncService;

    @PostMapping("/new-invoice")
    public ResponseEntity<Void> newInvoice(@RequestBody String senal,
                                        @RequestParam("authorization") String authorization) throws SQLException {
        log.info("[ ============================================ ]");
        log.info("[ =   S T A R T      N E W - I N V O I C E   = ]");
        log.info("[ =                  A L E G R A             = ]");
        log.info("[ ============================================ ]");
        log.debug("[ WEBHOOK ] Received request for process: {}", new Gson().toJson(senal));
        if(null == authorization ||   null == senal){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        byte[] decodedBytes = Base64.getDecoder().decode(authorization);
        String decodedString = new String(decodedBytes);
        log.info(decodedString);
        if(decodedString.split(":").length != 2){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String idCliente = decodedString.split(":")[0];
        String accessKey = decodedString.split(":")[1];

        Cliente u = userDataRepository.findByClientAndAccess(idCliente, accessKey);
        if(null == u){
            log.warn("Se ignora la informacion por que cliente/acceso no corresponden : {}", idCliente);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
             saleAsyncService.procesoAsyncDelaVenta(senal, u, idCliente, accessKey);
        } catch (Exception e) {
            log.error("[ ERROR ] [ GENERAL] [ RECEPCION DE EVENTO ][ WEBHOOK ]  {}", senal);
        }
        log.debug("[ WEBHOOK ] Request procesed: {}", new Gson().toJson(senal));
        log.info("[ ======================================== ]");
        log.info("[ =   E N D      N E W - I N V O I C E   = ]");
        log.info("[ ======================================== ]");
        return ResponseEntity.status(HttpStatus.OK).build();
    }
    @PostMapping("/new-client")
    public ResponseEntity<Void> newClient(@RequestBody String senal,
                                        @RequestParam("authorization") String authorization) throws SQLException {
        log.info("[ =========================================== ]");
        log.info("[ =   S T A R T      N E W - C L I E N T    = ]");
        log.info("[ =                  A L E G R A            = ]");
        log.info("[ =========================================== ]");
        log.debug("[ WEBHOOK ] Received request for process: {}", new Gson().toJson(senal));
        if(null == authorization ||   null == senal){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        byte[] decodedBytes = Base64.getDecoder().decode(authorization);
        String decodedString = new String(decodedBytes);
        log.info(decodedString);
        if(decodedString.split(":").length != 2){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        String idCliente = decodedString.split(":")[0];
        String accessKey = decodedString.split(":")[1];

        Cliente u = userDataRepository.findByClientAndAccess(idCliente, accessKey);
        if(null == u){
            log.warn("Se ignora la informacion por que cliente/acceso no corresponden : {}", idCliente);
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            clientAsyncService.procesoAsyncDelClienteNuevo(senal, u, idCliente, accessKey);
        } catch (Exception e) {
            log.error("[ ERROR ] [ GENERAL] [ RECEPCION DE EVENTO ][ WEBHOOK ]  {}", senal);
        }
        log.debug("[ WEBHOOK ] Request procesed: {}", new Gson().toJson(senal));
        log.info("[ ====================================== ]");
        log.info("[ =   E N D      N E W - C L I E N T   = ]");
        log.info("[ ====================================== ]");
        return ResponseEntity.status(HttpStatus.OK).build();
    }


}