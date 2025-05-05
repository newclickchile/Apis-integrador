package cl.integrador.bsale.woowup.controller;

import cl.integrador.bsale.woowup.model.entity.Cliente;
import cl.integrador.bsale.woowup.model.entity.Mail;
import cl.integrador.bsale.woowup.repository.MailDataRepository;
import cl.integrador.bsale.woowup.repository.UserDataRepository;
import cl.integrador.bsale.woowup.service.ProofyService;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

    //public static final String REG_EXP_CMN_BASH = "[0-9A-Za-z@.*\"$'+( )<=|_:;{}\\/\\%-]+";

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

    /*@Value("${socket.gmail.port}")
    String socketgPort;
    @Value("${socket.hmail.port}")
    String socketyPort;
    @Value("${socket.ymail.port}")
    String sockethPort;
    @Value("${socket.omail.port}")
    String socketoPort;*/


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
            InetAddress ip = InetAddress.getLocalHost();
            String responseFromRedis = redisService.getValue (email);
            log.debug("[ La respuesta desde redis es : {} ]", responseFromRedis);
            if(null != responseFromRedis) {
                log.debug("[ El correo {} ya fue validado con anterioridad ]", email);
                registraBD( idCliente, fechaIngreso, email, responseFromRedis, ip.getHostAddress(), 0, 1);
                finLog();
                return ResponseEntity.status(HttpStatus.OK).body(responseFromRedis);
            }
            if(!isValidEmail(email)) {
                log.debug("[ El correo {} NO es valido. NO se invocarà al app socket ]", email);
                String msgResultado = "{ \"result\":\"" + HttpStatus.NOT_FOUND.value() + "\", " +
                        "\"message\":\"correo no valido.\", " +
                        "\"email\":\"" + email + "\", " +
                        "\"dominio\":\"" + getDominio(email)+
                        "\",\"new_mail\":\"0\"}";

                registraBD( idCliente, fechaIngreso, email, msgResultado, ip.getHostAddress(), 0, 0);
                finLog();
                return ResponseEntity.status(HttpStatus.OK).body(msgResultado);
            }
            // Revisa el dominio si es valido y donde se debe validar
            String valdominio=isValidDomain(getDominio(email));
            // Para dominios no valido el mensaje:"{ \"result\":404, \"aplicacion\":\"\", \"Server\":\"\", \"socket\":\"\" }";
            JsonObject jsonObject = JsonParser.parseString(valdominio).getAsJsonObject();
            Integer resultado = jsonObject.get("result").getAsInt();
            if(resultado.equals(404)) {
                log.debug("[ El dominio {} NO es valido. NO se invocarà al app socket ]", getDominio(email));
                String msgResultado = "{ \"result\":\"" + HttpStatus.NOT_FOUND.value() + "\", " +
                        "\"message\":\"dominio no valido.\", " +
                        "\"email\":\"" + email + "\", " +
                        "\"dominio\":\"" + getDominio(email)+
                        "\",\"new_mail\":\"1\"}";
                // registramos el mail en Redis
                redisService.setValue(email,msgResultado);
                registraBD( idCliente, fechaIngreso, email, msgResultado, ip.getHostAddress(), 0,0);
                finLog();
                return ResponseEntity.status(HttpStatus.OK).body(msgResultado);
            }else {
                // Para dominios validos y validacion API el mensaje:"{ \"result\":408, \"aplicacion\":\"API\", \"server\":\"127.0.0.1\", \"socket\":\"15557\" }";
                String aplicacion = jsonObject.get("aplicacion").getAsString();
                if (aplicacion.equals("API")) {
                    String tokenApi = jsonObject.get("token").getAsString();
                    String result;
                    String mensaje;
                    // Invocamos a la API de validacion externa
                    try {
                        String responseBody = ProofyService.validaEmailProofy(email, tokenApi);
                        log.debug("[ VAR ] Result checkMail : {} - {}", email, responseBody);
                        JsonObject JsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                        String status = JsonResponse.get("status").getAsString();
                        if (status.equals("valid") || status.equals("risky")){
                            result="200";
                            mensaje="correo valido";
                        }
                        else if (status.isEmpty()){
                            result="408";
                            mensaje="correo no se pudo validar";
                        }
                        else{
                            result="404";
                            mensaje="correo no valido";
                        }

                        String msgResultado = "{ \"result\":\"" + result + "\", " +
                                "\"message\":\"" + mensaje + "\", " +
                                "\"email\":\"" + email + "\", " +
                                "\"dominio\":\"" + getDominio(email)+
                                "\",\"new_mail\":\"1\"}";

                        // registramos el mail en Redis si no es 408 (no se pudo validar)
                        if (!result.equals("408")){
                            redisService.setValue(email, msgResultado);
                        }
                        registraBD(idCliente, fechaIngreso, email, msgResultado, ip.getHostAddress(), 0, 0);
                        finLog();
                        return ResponseEntity.status(HttpStatus.OK).body(msgResultado);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    // Para dominios validos y validacion Socket el mensaje:"{ \"result\":408, \"aplicacion\":\"Socket\", \"server\":\"127.0.0.1\", \"socket\":\"15557\" }";
                    String Server = jsonObject.get("server").getAsString();
                    String Socket = jsonObject.get("socket").getAsString();
                    // Enviamos el mensaje al Socket
                    String responseBody = envioMsgAlSocket(email, Server, Socket);
                    log.debug("[ VAR ] Result socket : {} - {}", email, responseBody);
                    // registramos el mail en Redis
                    redisService.setValue(email, responseBody);
                    registraBD(idCliente, fechaIngreso, email, responseBody, ip.getHostAddress(), Integer.parseInt(Socket), 0);
                    finLog();
                    return ResponseEntity.status(HttpStatus.OK).body(responseBody);
                }
            }
        } catch (Exception e) {
            log.error("[ ERROR ] [ GENERAL] [ RECEPCION DE EVENTO ][ WEBHOOK ]  {}", email);
            log.error("[ ERROR ] [ GENERAL] [ RECEPCION DE EVENTO ]   {}", e.getMessage());
            String msgError = "{ \"result\":\"" + HttpStatus.INTERNAL_SERVER_ERROR.value() + "\", " +
                "\"message\":\"Error en el proceso de validacion de correo.\", " +
                "\"email\":\"" + email + "\", " +
                "\"dominio\":\"" + getDominio(email)+
                "\",\"new_mail\":\"1\"}";
            registraBD( idCliente, fechaIngreso, email, msgError, "127.0.0.1", 0, 0);
            finLog();
            return ResponseEntity.status(HttpStatus.OK).body(msgError);
        }
    }
    private String envioMsgAlSocket( String dato, String Server, String Socket) {
        log.debug("[ INFO ] [ Enviando al socket el mensaje {} ]", dato);
        try {
            log.debug("[ VAR ] [ IP, PORT Socket : {} {}", Server, Socket);
            ClienteSocket client =new ClienteSocket();
            client.startConnection(Server, Integer.parseInt(Socket));
            return client.sendMessage(socketTokenAuth.concat("#").concat(dato));
        } catch (UnknownHostException e) {
            log.error("[ ATENCION ][ No se pudo enviar el mensaje por error en el HOST : {} ]", e.getMessage());
        } catch (IOException e) {
            log.error("[ ATENCION ][ No se pudo enviar el mensaje por eror I/O: {} ]", e.getMessage());
        }
        log.error("[ ERROR ] [ No se pudo enviar al socket el mensaje {}#{}#{}#{} ]", Server, Socket,  socketTokenAuth,  dato);
        return null;
    }

    /*private String getSocketSegunCorreo(String correo) {
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
    }*/


    private void finLog(){
        log.info("[ ================================ ]");
        log.info("[ =   E N D      W E B H O O K   = ]");
        log.info("[ ================================ ]");
    }

    private void registraBD( String idCliente, Date fechaIngreso, String email, String Data, String Ip, Integer socket, Integer new_mail){
        Mail m = new Mail();
        JsonObject jsonObject = JsonParser.parseString(Data).getAsJsonObject();
        String result = jsonObject.get("result").getAsString();
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
    }
    public static String isValidDomain(String email) {
        StringBuilder output = new StringBuilder();
        String command="/validDominio.sh ".concat(email);
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
            //e.printStackTrace();
            output.append("An error occurred: ").append(e.getMessage()).append("\n");
        }

        return output.toString();
    }

}