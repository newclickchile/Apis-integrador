package cl.integrador.bsale.woowup.controller;

import cl.integrador.bsale.woowup.model.entity.Cliente;
import cl.integrador.bsale.woowup.model.entity.ClienteSucursal;
import cl.integrador.bsale.woowup.model.pojo.Local;
import cl.integrador.bsale.woowup.model.pojo.Senal;
import cl.integrador.bsale.woowup.repository.UserDataRepository;
import cl.integrador.bsale.woowup.repository.UserSucursalDataRepository;
import cl.integrador.bsale.woowup.service.Bsale2Service;
import cl.integrador.bsale.woowup.service.WoowUp2Service;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/v1")
@Slf4j
public class ConfigController {

    @Autowired
    private UserSucursalDataRepository userSucursalDataRepository;
    @Autowired
    private UserDataRepository userDataRepository;
    @Autowired
    private Bsale2Service bsaleService;
    @Autowired
    private WoowUp2Service woowUpService;

    @PostMapping("/")
    public ResponseEntity<String> webhook(@RequestBody Senal senal,
                                        @RequestHeader("Cliente") String idCliente,
                                        @RequestHeader("Access_key") String accessKey,
                                        @RequestHeader("Content-Type") String contentType) throws SQLException {
        log.info("[ ==================================== ]");
        log.info("[ =   S T A R T      W E B H O O K   = ]");
        log.info("[ ==================================== ]");
        log.debug("[ WEBHOOK ] Received request for process: {}", new Gson().toJson(senal));
        if(null == idCliente || null == accessKey|| null == senal){
            finLog();
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        Cliente u = userDataRepository.findByClientAndAccess(idCliente, accessKey);
        if(null == u){
            log.warn("Se ignora la informacion por que cliente/acceso no corresponden : {}", idCliente);
            finLog();
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if(validaDatosEntrada(senal)){
            log.warn("Faltan datos para procesar la peticion : {}", idCliente);
            finLog();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        u = userDataRepository.findByClientAndAccess(senal.getClientId(), senal.getSecretKeyBsale(), senal.getKeyWoowup());
        if(null != u){
            log.warn("Ya existen esos datos para : {}", idCliente);
            finLog();
            return new ResponseEntity<>(HttpStatus.ALREADY_REPORTED);
        }
        String dynamicString = "";
        try {
            log.debug("[ VAR ] Valida KeyBasale");
            HttpStatusCode code = bsaleService.validaKeyBsale(senal.getSecretKeyBsale());
            if(code.value() == HttpStatus.INTERNAL_SERVER_ERROR.value()){
                log.warn("Token no autorizado para Bsale : {}", idCliente);
                finLog();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Token Bsale no es vàlido\"}");
            }

            log.debug("[ VAR ] Valida Keywowup");
            code = woowUpService.validaKeyWoowup(senal.getKeyWoowup());
            if(code.value() == HttpStatus.INTERNAL_SERVER_ERROR.value()){
                log.warn("Token no autorizado para Woowup : {}", idCliente);
                finLog();
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"error\":\"Token Woowup no es vàlido\"}");
            }
            log.debug("[ PROCESS ] Insert Client: {}", senal.getClientId());
            dynamicString = generateRandomString(25);
            Cliente c = new Cliente();
            c.setAccessKey(dynamicString);
            c.setHabilitado(true);
            c.setEmailValidate(senal.isEmailValidate());
            c.setPais(senal.getCodigoPais());
            c.setIdCliente(senal.getClientId());
            c.setKeyBsale(senal.getSecretKeyBsale());
            c.setKeyWoowup(senal.getKeyWoowup());
            c.setOfficeWeb(senal.getOfficeWeb());
            userDataRepository.save(c);

            List<ClienteSucursal> listCs = new ArrayList<>();
            for(Local l: senal.getOffices()){
                log.debug("[ PROCESS ] Insert Client {}, Local : {}", senal.getClientId(), l.getId());
                ClienteSucursal cs = new ClienteSucursal();
                cs.setHabilitado(true);
                cs.setIdCliente(senal.getClientId());
                cs.setIdSucursal(l.getId());
                listCs.add(cs);
            }
            userSucursalDataRepository.saveAll(listCs);

            log.debug("[ WEBHOOK ] Request procesed: {}", new Gson().toJson(senal));
            finLog();
            String responseBody = "{ \"URL\": \"https://apis.sti-plus.cl/int-bsale-woowup-1.0.0/v1/\", " +
                    "\"Header\": '{\"Cliente\":\"" + senal.getClientId() + "\"," +
                    "\"Access_key\":\"" + dynamicString + "\"," +
                    "\"Content-Type\":\"application/json\"}'\n" +
                    " }";
            return ResponseEntity.status(HttpStatus.OK).body(responseBody);
        } catch (Exception e) {
            log.error("[ ERROR ] [ GENERAL] [ RECEPCION DE EVENTO ][ WEBHOOK ]  {}", senal.getClientId());
        }
        finLog();
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validaDatosEntrada(Senal senal) {
        return null == senal
                || null == senal.getClientId()
                || null == senal.getCodigoPais()
                || null == senal.getKeyWoowup()
                || null == senal.getSecretKeyBsale() ;
    }


    private static String generateRandomString(int length) {
        String characters = "abcdefghijklmnopqrstuvwxyz" +
                "0123456789" +
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }
    private void finLog(){
        log.info("[ ================================ ]");
        log.info("[ =   E N D      W E B H O O K   = ]");
        log.info("[ ================================ ]");
    }

}