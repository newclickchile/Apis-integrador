package cl.integrador.bsale.woowup.controller;

import cl.integrador.bsale.woowup.model.entity.Cliente;
import cl.integrador.bsale.woowup.model.entity.Mail;
import cl.integrador.bsale.woowup.repository.MailDataRepository;
import cl.integrador.bsale.woowup.repository.UserDataRepository;
import cl.integrador.bsale.woowup.service.RedisService;
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
    @Autowired
    private RedisService redisService;

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
    @Value("${socket.omail.port}")
    String socketoPort;


    @GetMapping("/check")
    public ResponseEntity<String> checkMail(@RequestParam String email,
                                        @RequestHeader("Cliente") String idCliente,
                                        @RequestHeader("Access_key") String accessKey,
                                        @RequestHeader("Content-Type") String contentType) throws SQLException {
        log.info("[ ==================================== ]");
        log.info("[ =   S T A R T      W E B H O O K   = ]");
        log.info("[ ==================================== ]");
        log.debug("[ WEBHOOK ] Received request for process: {}", email);
        Date fechaIngreso = new Date();
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
                log.debug("[ El correo {} NO es valido. NO se invocarà al app socket ]", email);
                String msgError = "{ \"result\":\"" + HttpStatus.NOT_FOUND.value() + "\", " +
                        "\"message\":\"correo no valido.\", " +
                        "\"email\":\"" + email + "\", " +
                        "\"dominio\":\"" + getDominio(email)+
                        "\",\"new_mail\":\"0\"}";

                InetAddress ip = InetAddress.getLocalHost();
                registraBD( idCliente, fechaIngreso, email, msgError, ip.getHostAddress(), 0);
                finLog();
                return ResponseEntity.status(HttpStatus.OK).body(msgError);
            }
            String responseFromRedis = redisService.getValue (email);
            log.debug("[ La respuesta desde redis es : {} ]", responseFromRedis);
            if(null != responseFromRedis) {
                log.debug("[ El correo {} ya fue validado con anterioridad ]", email);
                InetAddress ip = InetAddress.getLocalHost();
                registraBD( idCliente, fechaIngreso, email, responseFromRedis, ip.getHostAddress(), 0);
                finLog();
                return ResponseEntity.status(HttpStatus.OK).body(responseFromRedis);
            }
            InetAddress ip = InetAddress.getLocalHost();
            String responseBody = envioMsgAlSocket(email) ;
            log.debug("[ VAR ] Result checkMail : {} - {}", email, responseBody);
            registraBD( idCliente, fechaIngreso, email, responseBody, ip.getHostAddress(), Integer.parseInt(getSocketSegunCorreo(email)));
            finLog();
            return ResponseEntity.status(HttpStatus.OK).body(responseBody);
        } catch (Exception e) {
            log.error("[ ERROR ] [ GENERAL] [ RECEPCION DE EVENTO ][ WEBHOOK ]  {}", email);
            log.error("[ ERROR ] [ GENERAL] [ RECEPCION DE EVENTO ]   {}", e.getMessage());
            String msgError = "{ \"result\":\"" + HttpStatus.INTERNAL_SERVER_ERROR.value() + "\", " +
                "\"message\":\"Error en el proceso de validacion de correo.\", " +
                "\"email\":\"" + email + "\", " +
                "\"dominio\":\"" + getDominio(email)+
                "\",\"new_mail\":\"1\"}";
            registraBD( idCliente, fechaIngreso, email, msgError, "127.0.0.1", 0);
            finLog();
            return ResponseEntity.status(HttpStatus.OK).body(msgError);
        }
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
            log.error("[ ATENCION ][ No se pudo enviar el mensaje por eror I/O: {} ]", e.getMessage());
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
        if(correo.contains("outlook")){
            return socketoPort;
        }
        return socketPort;
    }


    private void finLog(){
        log.info("[ ================================ ]");
        log.info("[ =   E N D      W E B H O O K   = ]");
        log.info("[ ================================ ]");
    }

    private void registraBD( String idCliente, Date fechaIngreso, String email, String Data, String Ip, Integer socket){
        Mail m = new Mail();
        JsonObject jsonObject = JsonParser.parseString(Data).getAsJsonObject();
        String result = jsonObject.get("result").getAsString();
        Integer new_mail = jsonObject.get("new_mail").getAsInt();
        m.setIdCliente(idCliente);
        m.setResultado(result);
        m.setFechaIngreso(fechaIngreso);
        m.setFechaTermino(new Date());
        m.setServer(Ip);
        m.setMail(email);
        m.setDataSalida(Data);
        m.setDominio(getDominio(email));
        m.setMailNew(new_mail);
        m.setSocket(socket);
        mailDataRepository.save(m);
        return;
    }

}