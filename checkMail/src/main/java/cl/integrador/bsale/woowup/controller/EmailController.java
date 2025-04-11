package cl.integrador.bsale.woowup.controller;

import cl.integrador.bsale.woowup.model.entity.Cliente;
import cl.integrador.bsale.woowup.model.entity.Mail;
import cl.integrador.bsale.woowup.repository.MailDataRepository;
import cl.integrador.bsale.woowup.repository.UserDataRepository;
import cl.integrador.bsale.woowup.util.ClienteSocket;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Date;

import static cl.integrador.bsale.woowup.util.EmailValidator.getDominio;
import static cl.integrador.bsale.woowup.util.EmailValidator.isValidEmail;

@RestController
@RequestMapping("/v1")
@Slf4j
public class EmailController {

    public static final String REG_EXP_CMN_BASH = "[0-9A-Za-z@.*\"$'+( )<=|_:;{}\\/\\%-]+";

    @Autowired
    UserDataRepository userDataRepository;
    @Autowired
    MailDataRepository mailDataRepository;

    @Value("${socket.mail.ip}")
    String socketIP;
    @Value("${socket.mail.port}")
    String socketPort;
    @Value("${socket.mail.token.auth}")
    String socketTokenAuth;

    @Value("${socket.gmail.port}")
    String socketgPort;
    @Value("${socket.hmail.port}")
    String socketyPort;
    @Value("${socket.ymail.port}")
    String sockethPort;


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
            if(!isValidEmail(email)) {
                String msgError = "{ \"result\":\"" + HttpStatus.BAD_REQUEST + "\", " +
                        "\"message\":\"correo no valido.\", " +
                        "\"email\":\"" + email + "\", " +
                        "\"dominio\":\"" + getDominio(email)+
                        "\",\"new_mail\":\"1\"}";
                return ResponseEntity.status(HttpStatus.OK).body(msgError);
            }

            InetAddress ip = InetAddress.getLocalHost();
            String responseBody = envioMsgAlSocket(email) ;
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
        String msgError = "{ \"result\":\"" + HttpStatus.INTERNAL_SERVER_ERROR + "\", " +
                "\"message\":\"Error en el proceso de validacion de correo.\", " +
                "\"email\":\"" + email + "\", " +
                "\"dominio\":\"" + getDominio(email)+
                "\",\"new_mail\":\"1\"}";
        return ResponseEntity.status(HttpStatus.OK).body(msgError);
    }
    private String envioMsgAlSocket( String dato) {
        log.debug("[ INFO ] [ Enviando al socket el mensaje {} ]", dato);
        try {
            log.debug("[ VAR ] [ IP, PORT Socket : {} {}", socketIP, socketPort);
            ClienteSocket client =new ClienteSocket();
            client.startConnection(socketIP, Integer.parseInt(getSocketSegunCorreo(dato)));
            return client.sendMessage(socketTokenAuth.concat("#").concat(dato));
        } catch (UnknownHostException e) {
            log.error("[ ATENCION ][ No se pudo enviar el mensaje por error en el HOST : {} ]", e.getMessage());
        } catch (IOException e) {
            log.error("[ ATENCION ][ No se pudo enviar el mensaje por errror I/O: {} ]", e.getMessage());
        }
        log.error("[ ERROR ] [ No se pudo enviar al socket el mensaje {}#{}#{}#{} ]", socketIP, socketPort,  socketTokenAuth,  dato);
        return null;
    }

    private String getSocketSegunCorreo(String correo) {
        if(correo.contains("gmail")){
            return socketgPort;
        }
        if(correo.contains("yahoo")){
            return socketyPort;
        }
        if(correo.contains("hotmail")){
            return sockethPort;
        }
        return socketPort;
    }


    private void finLog(){
        log.info("[ ================================ ]");
        log.info("[ =   E N D      W E B H O O K   = ]");
        log.info("[ ================================ ]");
    }

}