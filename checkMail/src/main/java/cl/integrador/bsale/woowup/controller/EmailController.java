package cl.integrador.bsale.woowup.controller;

import cl.integrador.bsale.woowup.model.entity.Cliente;
import cl.integrador.bsale.woowup.model.entity.Mail;
import cl.integrador.bsale.woowup.repository.MailDataRepository;
import cl.integrador.bsale.woowup.repository.UserDataRepository;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Date;

@RestController
@RequestMapping("/v1")
@Slf4j
public class EmailController {

    public static final String REG_EXP_CMN_BASH = "[0-9A-Za-z@.*\"$'+( )<=|_:;{}\\/\\%-]+";

    @Autowired
    UserDataRepository userDataRepository;
    @Autowired
    MailDataRepository mailDataRepository;


    @GetMapping("/check")
    public ResponseEntity<String> checkMail(@RequestParam String email,
                                        @RequestHeader("Cliente") String idCliente,
                                        @RequestHeader("Access_key") String accessKey,
                                        @RequestHeader("Content-Type") String contentType) throws SQLException {
        log.info("[ ==================================== ]");
        log.info("[ =   S T A R T      W E B H O O K   = ]");
        log.info("[ ==================================== ]");
        log.debug("[ WEBHOOK ] Received request for process: {}", email);
        if(null == idCliente || null == accessKey || null == email){
            finLog();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Cliente u = userDataRepository.findByClientAndAccess(idCliente, accessKey);
        if(null == u){
            log.warn("Se ignora la informacion por que cliente/acceso no corresponden : {}", idCliente);
            finLog();
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            InetAddress ip = InetAddress.getLocalHost();
            String responseBody =  executeCommand("/opt/apache-tomcat-11.0.2/work/validMail.sh " + email);
            JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
            String result = jsonObject.get("result").getAsString();
            log.debug("[ VAR ] Result checkMail : {} - {}", email, responseBody);
            Mail m = new Mail();
            m.setIdCliente(idCliente);
            m.setResultado(result);
            m.setFechaIngreso(new Date());
            m.setServer(ip.getHostAddress());
            m.setMail(email);
            m.setDataSalida(responseBody);
            mailDataRepository.save(m);
            finLog();
            return ResponseEntity.status(HttpStatus.OK).body(responseBody);
        } catch (Exception e) {
            log.error("[ ERROR ] [ GENERAL] [ RECEPCION DE EVENTO ][ WEBHOOK ]  {}", email);
            log.error("[ ERROR ] [ GENERAL] [ RECEPCION DE EVENTO ]   {}", e.getMessage());
        }
        finLog();
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public static String executeCommand(String command) {
        StringBuilder output = new StringBuilder();

        try {
            Process process = new ProcessBuilder("/bin/sh", "-c", command).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            reader.close();

            while ((line = errorReader.readLine()) != null) {
                output.append("ERROR: ").append(line).append("\n");
            }
            errorReader.close();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                output.append("Command exited with code ").append(exitCode).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            output.append("An error occurred: ").append(e.getMessage()).append("\n");
        }

        return output.toString();
    }



    private void finLog(){
        log.info("[ ================================ ]");
        log.info("[ =   E N D      W E B H O O K   = ]");
        log.info("[ ================================ ]");
    }

}