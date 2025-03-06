package cl.integrador.alegra.woowup.service;

import cl.integrador.alegra.woowup.model.entity.Cliente;
import cl.integrador.alegra.woowup.model.entity.ClienteSucursal;
import cl.integrador.alegra.woowup.model.entity.Log;
import cl.integrador.alegra.woowup.model.pojo.ClienteWoowup;
import cl.integrador.alegra.woowup.model.pojo.VentaWoowup;
import cl.integrador.alegra.woowup.model.pojo.WoowupResponse;
import cl.integrador.alegra.woowup.repository.LogRepository;
import cl.integrador.alegra.woowup.repository.UserDataRepository;
import cl.integrador.alegra.woowup.repository.UserSucursalDataRepository;
import cl.integrador.alegra.woowup.util.IntegrationHelper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

@Slf4j
@Service
public class ClientAsyncService {

    @Autowired
    private UserDataRepository userDataRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private UserSucursalDataRepository userSucursalDataRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private Alegra2Service alegra2Service;

    @Autowired
    private WoowUp2Service woowUp2Service;

    @Autowired
    private RedisService redisService;

    @Value("${check.mail}")
    private String checkAllIncomingMail;



    @Async("asyncTaskExecutor")
    public void procesoAsyncDelClienteNuevo(String jsonAlegra, Cliente clienteBD, String idCliente, String accessKey) {
        Long idDataLog = crearDataLog(jsonAlegra, idCliente);
        escribeLogStartEnd(idDataLog, true);
        log.debug("{}[ VAR ] Buscando los datos del cliente : {}", idDataLog, idCliente);
        JsonObject jsonObject = JsonParser.parseString(jsonAlegra).getAsJsonObject();
        String id = jsonObject
                .getAsJsonObject("message")
                .getAsJsonObject("client")
                .get("identification")
                .getAsString();
        if(!esTipoCliente( jsonAlegra )){
            log.warn("{} Se ignora la informacion por que el type no es 'client'",
                    idDataLog, id);
            cerrarUnDataLog(idDataLog, "OK", "Se ignora informacion. Type no es 'client'", "");
        }else {
            if (!redisService.validarResource(id)) {
                procesarEventoCliente(idDataLog, jsonAlegra, clienteBD);
            } else {
                log.warn("{} Se ignora la informacion por que el resource {} ya fue procesado",
                        idDataLog, id);
                cerrarUnDataLog(idDataLog, "OK", "Se ignora informacion. Resource ya fue informado", "");
            }
        }
        escribeLogStartEnd(idDataLog, false);
    }



    private void procesarEventoCliente(Long idDataLog, String jsonAlegra, Cliente clienteBD) {
        HttpStatus res = procesoCreacionActualizacionDecliente(idDataLog, jsonAlegra, clienteBD);
        switch (res) {
            case HttpStatus.OK:
                break;
            case HttpStatus.FORBIDDEN:
                log.warn("{} Se ignora informaciòn. Falta el nodo 'Client'", idDataLog);
                cerrarUnDataLog(idDataLog, "OK", "Se ignora informacion. No viene dato del cliente", "");
                break;
            case HttpStatus.BAD_REQUEST:
                log.warn("{} Se continua con la venta a pesar de que el correo NO es valido", idDataLog);
                ingresarLaVenta(idDataLog, jsonAlegra, clienteBD);
                break;
            default:
                log.error("{} Error en el proceso de Crear/Actualizar cliente", idDataLog);
                cerrarUnDataLog(idDataLog, "NOK", res + " Error en el proceso de Crear/Actualizar cliente", "");
                break;
        }
    }



    private void ingresarLaVenta(Long idDataLog, String jsonAlegra, Cliente u) {
        log.debug("{}[ PROCESS ] ingresarLaVenta: {}", idDataLog, u.getIdCliente());
        Gson gson = new Gson();
        VentaWoowup vw = IntegrationHelper.getObjectVentaWoowump(jsonAlegra);
        WoowupResponse httpCode = woowUp2Service.ingresarVenta(vw, u.getKeyWoowup());
        if(httpCode.getResultCode() == HttpStatus.OK.value()){
            cerrarUnDataLog(idDataLog, "OK",String.valueOf(HttpStatus.OK.value())
                    , StringEscapeUtils.unescapeJava( gson.toJson(vw) ));
        }else {
            JsonObject msgRes = JsonParser.parseString(httpCode.getMessage()).getAsJsonObject();
            cerrarUnDataLog(idDataLog, "NOK",httpCode.getResultCode() + " " +
                            msgRes.get("message").getAsString(),
                    StringEscapeUtils.unescapeJava( gson.toJson(vw) ));
        }
    }

    private HttpStatus procesoCreacionActualizacionDecliente(long idDataLog, String jsonAlegra, Cliente u) {
        boolean emailValido = validarEmailDelCliente(jsonAlegra, u);
        ClienteWoowup cw = IntegrationHelper.getObjectClientWoowup(jsonAlegra, u, emailValido);
        if (cw == null) {
            return HttpStatus.FORBIDDEN;
        }
        HttpStatusCode codeResponse = woowUp2Service.existeCliente(cw, u.getKeyWoowup());
        if (codeResponse.value() == 200) {
            Gson gson = new Gson();
            cerrarUnDataLog(idDataLog, "OK",String.valueOf(HttpStatus.OK.value())
                    , StringEscapeUtils.unescapeJava( gson.toJson(cw) ));

        }



        return manejarRespuestaDelServicio(cw, codeResponse, u);
    }

    private boolean validarEmailDelCliente(String jsonAlegra, Cliente u) {
        JsonObject jsonObject = JsonParser.parseString(jsonAlegra).getAsJsonObject();
        String email = jsonObject
                .getAsJsonObject("message")
                .getAsJsonObject("client")
                .get("email")
                .getAsString();
        boolean emailValido = isEmailValidoDelCliente(email, u);

        if (!emailValido) {
            log.warn("[ ATENCION ] el email {} no es valido segun servicio externo checkMail", email);
        }

        return emailValido;
    }

    private HttpStatus manejarRespuestaDelServicio(ClienteWoowup cw, HttpStatusCode codeResponse, Cliente u) {
        switch (codeResponse) {
            case HttpStatus.OK:
                log.debug("[ VAR ] Cliente existe ? {}", true);
                if (woowUp2Service.actualizaCliente(cw, u.getKeyWoowup())) {
                    return HttpStatus.OK;
                }
                break;
            case HttpStatus.NOT_FOUND:
                log.debug("[ VAR ] Cliente existe ? {}", false);
                if (woowUp2Service.creaCliente(cw, u.getKeyWoowup())) {
                    return HttpStatus.OK;
                }
                break;
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }


    private boolean isEmailValidoDelCliente(String email, Cliente u) {
        if( null == email || email.trim().length() == 0){
            log.debug("[ ATENCION ] No viene el dato 'correo'");
            return true;
        }
        if( checkAllIncomingMail.equalsIgnoreCase("0")){
            log.debug("[ ATENCION ] Esta configurado NO validar ningun correo y menos el {} ", email);
            return true;
        }
        if( !u.isEmailValidate()){
            log.debug("[ ATENCION ] El cliente {} tiene configurado no validar correos como {} ", u.getIdCliente(), email);
            return true;
        }

        String resValidacionMail = emailService.getCheckEmailInfo(u.getIdCliente(), u.getAccessKey(), email);
        if(null == resValidacionMail){
            log.debug("[ ERROR ] no se pudo validar el email {} ", email);
            return false;
        }
        JsonObject jsonObject = JsonParser.parseString(resValidacionMail).getAsJsonObject();
        String result = jsonObject.get("result").getAsString();
        log.debug("[ VAR ] checkMail resultado para {} es : {} ", email, result);
        if(result.equalsIgnoreCase(String.valueOf(HttpStatus.OK.value()))){
            log.debug("[ OK ] El email {} es vàlido ", email);
            return true;
        }else{
            log.debug("[ NOK ] El email {} NO es vàlido ", email);
            return false;
        }

    }


    private Long crearDataLog(String senal, String idCliente) {
        Log l = new Log();
        l.setIdCliente(idCliente);
        l.setIdAplicativo("INT-ALEGRA-WOOWUP");
        l.setServer(getIpServer());
        l.setSistemaOrigen("Alegra");
        l.setFechaIngreso(new Date());
        l.setDataOrigen(  senal );
        l.setSistemaDestino("WOOWUP");
        l.setResultado("CREADO");
        Log savedLog = logRepository.save(l);
        log.debug("[ VAR ] ID objeto data-log : {}" , savedLog.getId());
        return savedLog.getId();
    }
    private void cerrarUnDataLog(Long idDataLog, String resultado, String obs, String dataDestino){
        log.debug("[ PROCESS ] Cerrando data-log con id: {}" , idDataLog);
        Log l = logRepository.getByIdDeLog(idDataLog);
        l.setFechaDestino(new Date());
        l.setResultado(resultado);
        l.setObservacion(obs);
        l.setDataDestino(dataDestino);
        logRepository.save(l);
    }
    private String getIpServer() {
        String ipServer = "";
        try {
            InetAddress ip = InetAddress.getLocalHost();
            ipServer = ip.getHostAddress();
        } catch (UnknownHostException e) {
            log.error("No se pudo obtener la IP del servidor: {}" , e.getMessage());
        }
        return ipServer;
    }

    private boolean esTipoCliente(String jsonAlegra ) {
        JsonObject jsonObject = JsonParser.parseString(jsonAlegra).getAsJsonObject();
        String tipoCliente = jsonObject
                .getAsJsonObject("message")
                .getAsJsonObject("client")
                .get("type")
                .getAsJsonArray().get(0).getAsString();
        return tipoCliente.equals("client");
    }

    private void escribeLogStartEnd(Long idDataLog, boolean start) {
        log.debug("{}[ ================================ ]", idDataLog);
        if(start){
            log.debug("{}[ =   S T A R T      A S Y N C   = ]", idDataLog);
        }else{
            log.debug("{}[ =   E N D      A S Y N C   = ]", idDataLog);
        }
        log.debug("{}[ ================================ ]", idDataLog);
    }
}
