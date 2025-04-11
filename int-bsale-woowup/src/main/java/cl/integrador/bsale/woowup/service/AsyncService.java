package cl.integrador.bsale.woowup.service;

import cl.integrador.bsale.woowup.model.entity.Cliente;
import cl.integrador.bsale.woowup.model.entity.ClienteSucursal;
import cl.integrador.bsale.woowup.model.entity.Log;
import cl.integrador.bsale.woowup.model.pojo.ClienteWoowup;
import cl.integrador.bsale.woowup.model.pojo.Senal;
import cl.integrador.bsale.woowup.model.pojo.VentaWoowup;
import cl.integrador.bsale.woowup.model.pojo.WoowupResponse;
import cl.integrador.bsale.woowup.model.pojo.bsale.BsaleResponse;
import cl.integrador.bsale.woowup.repository.LogRepository;
import cl.integrador.bsale.woowup.repository.UserDataRepository;
import cl.integrador.bsale.woowup.repository.UserSucursalDataRepository;
import cl.integrador.bsale.woowup.util.IntegrationHelper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;

@Slf4j
@Service
public class AsyncService {
    private static int CLIENTE_NUEVO = 1;
    private static int CLIENTE_EXISTE = 0;

    @Autowired
    private UserDataRepository userDataRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private UserSucursalDataRepository userSucursalDataRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private Bsale2Service bsale2Service;

    @Autowired
    private WoowUp2Service woowUp2Service;

    @Autowired
    private RedisService redisService;

    @Value("${check.mail}")
    private String checkAllIncomingMail;

    @Async("asyncTaskExecutor")
    public void procesoAsyncDelEvento(Senal senal, Cliente clienteBD, String idCliente, String accessKey) {
        Long idDataLog = crearDataLog(senal, idCliente);
        log.debug("{}[ ================================ ]", idDataLog);
        log.debug("{}[ =   S T A R T      A S Y N C   = ]", idDataLog);
        log.debug("{}[ ================================ ]", idDataLog);
        log.debug("{}[ VAR ] Buscando los datos del cliente : {}", idDataLog, idCliente);
        if (!redisService.validarResource(senal.getResourceId())) {
            procesarEvento(idDataLog, senal, clienteBD, idCliente);
        } else {
            log.warn("{} Se ignora la informacion por que el resource {} ya fue procesado", idDataLog, senal.getResourceId());
            cerrarUnDataLog(idDataLog, "OK", "Se ignora informacion. Resource ya fue informado", "");
        }
        log.debug("{}[ ============================ ]", idDataLog);
        log.debug("{}[ =   E N D      A S Y N C   = ]", idDataLog);
        log.debug("{}[ ============================ ]", idDataLog);
    }

    private void procesarEvento(Long idDataLog, Senal senal, Cliente clienteBD, String idCliente) {
        Gson gson = new Gson();
        ClienteSucursal sucursal = userSucursalDataRepository.findByClientAndSucursal(idCliente, senal.getOfficeId(), true);
        if (sucursal == null) {
            procesarEventoDeEsaSucursal(idDataLog, senal, clienteBD, gson);
        } else {
            log.warn("{} Se ignora la informacion por que la sucursal {} no esta autorizada", idDataLog, senal.getOfficeId());
            cerrarUnDataLog(idDataLog, "NOK", "Se ignora la informacion por que la sucursal no esta autorizada",
                    "La sucursal no permitida es : " + senal.getOfficeId());
        }
    }

    private void procesarEventoDeEsaSucursal(Long idDataLog, Senal senal, Cliente clienteBD, Gson gson) {
        log.debug("{}[ VAR ] Llamando servicio Bsale : {}", idDataLog, senal.getResource().split("/")[2]);
        String jsonBsale = bsale2Service.getInfo(senal.getResource().split("/")[2], clienteBD.getKeyBsale(), 1);
        if (jsonBsale != null) {
            if (!esTipoPermitidoDeDocto(jsonBsale)) {
                manejarDocumentoNoPermitido(idDataLog, jsonBsale, gson);
            } else {
                manejarProcesoCliente(idDataLog, jsonBsale, clienteBD);
            }
        } else {
            log.error("{} Problemas al obtener info de BSALE de : {}", idDataLog, senal.getResource().split("/")[2]);
            cerrarUnDataLog(idDataLog, "NOK", "Error en el proceso de leer desde Bsale los datos del cliente", "");
        }
    }

    private void manejarDocumentoNoPermitido(Long idDataLog, String jsonBsale, Gson gson) {
        BsaleResponse bsaleResponse = gson.fromJson(jsonBsale, BsaleResponse.class);
        log.warn("{} Se ignora la informacion por que el tipo documento {} no esta permitido", idDataLog,
                bsaleResponse.getDocumentType().getUse());
        cerrarUnDataLog(idDataLog, "OK", "Se ignora informacion. Tipo documento no esta permitido",
                "Tipo documento no permitida es : " + bsaleResponse.getDocumentType().getUse());
    }

    private void manejarProcesoCliente(Long idDataLog, String jsonBsale, Cliente clienteBD) {
        HttpStatus res = procesoCreacionActualizacionDecliente(idDataLog, jsonBsale, clienteBD);
        switch (res) {
            case HttpStatus.OK:
                ingresarLaVenta(idDataLog, jsonBsale, clienteBD);
                break;
            case HttpStatus.FORBIDDEN:
                log.warn("{} Se ignora informaciòn. Falta el nodo 'Client'", idDataLog);
                Gson gson = new Gson();
                BsaleResponse bsaleResponse = gson.fromJson(jsonBsale, BsaleResponse.class);
                cerrarUnDataLog(idDataLog, "OK", "Se ignora informacion. No viene dato del cliente", bsaleResponse.getOffice().getName());
                break;
            case HttpStatus.BAD_REQUEST:
                log.warn("{} Se continua con la venta a pesar de que el correo NO es valido", idDataLog);
                ingresarLaVenta(idDataLog, jsonBsale, clienteBD);
                break;
            default:
                log.error("{} Error en el proceso de Crear/Actualizar cliente", idDataLog);
                cerrarUnDataLog(idDataLog, "NOK", res + " Error en el proceso de Crear/Actualizar cliente", "");
                break;
        }
    }



    private void ingresarLaVenta(Long idDataLog, String jsonBsale, Cliente u) {
        log.debug("{}[ PROCESS ] ingresarLaVenta: {}", idDataLog, u.getIdCliente());
        Gson gson = new Gson();
        BsaleResponse bsaleResponse = gson.fromJson(jsonBsale, BsaleResponse.class);
        VentaWoowup vw = IntegrationHelper.getObjectVentaWoowump(bsaleResponse, u);
        ArrayList<VentaWoowup.PurchaseDetail> newlistPd = new ArrayList<>();
        if(null != vw){
            for(VentaWoowup.PurchaseDetail pd: vw.getPurchase_detail()) {
                String llave = u.getIdCliente().concat("_SKU_").concat(pd.getSku());
                String redisSku = redisService.buscaSku (llave);
                if (null == redisSku ) {
                    String jsonProducto = bsale2Service.getProduct(pd.getBrand(), u.getKeyBsale());
                    if (null != jsonProducto) {
                         JsonObject orden = JsonParser.parseString(jsonProducto).getAsJsonObject();
                        String brand = orden.get("product").getAsJsonObject().get("name")
                                .getAsString().replace("\"", "");
                        pd.setBrand( brand);
                        redisService.insertSku (llave, brand);
                    }
                }else{
                    pd.setBrand( redisSku );
                }
                newlistPd.add(pd);
            }
        }
        vw.setPurchase_detail(newlistPd);
        WoowupResponse httpCode = woowUp2Service.ingresarVenta(vw, u.getKeyWoowup());
        if(httpCode.getResultCode() == HttpStatus.OK.value()){
            cerrarUnDataLog(idDataLog, "OK",String.valueOf(HttpStatus.OK.value())
                    ,   gson.toJson(vw)  );
        }else {
            JsonObject msgRes = JsonParser.parseString(httpCode.getMessage()).getAsJsonObject();
            cerrarUnDataLog(idDataLog, "NOK",httpCode.getResultCode() + " " +
                            msgRes.get("message").getAsString(),
                      gson.toJson(vw)  );
        }
    }

    private HttpStatus procesoCreacionActualizacionDecliente(Long idDataLog, String jsonBsale, Cliente u) {
        Gson gson = new Gson();
        BsaleResponse bsaleResponse = gson.fromJson(jsonBsale, BsaleResponse.class);

        boolean emailValido = validarEmailDelCliente(bsaleResponse, u);
        ClienteWoowup cw = IntegrationHelper.getObjectClientWoowup(bsaleResponse, u, emailValido);

        if (cw == null) {
            return HttpStatus.FORBIDDEN;
        }

        HttpStatusCode codeResponse = woowUp2Service.existeCliente(cw, u.getKeyWoowup());
        return manejarRespuestaDelServicio(idDataLog, cw, codeResponse, u);
    }

    private boolean validarEmailDelCliente(BsaleResponse bsaleResponse, Cliente u) {
        String email = (bsaleResponse.getClient() != null) ? bsaleResponse.getClient().getEmail() : "";
        boolean emailValido = isEmailValidoDelCliente(email, u);

        if (!emailValido) {
            log.warn("[ ATENCION ] el email {} no es valido segun servicio externo checkMail", email);
        }

        return emailValido;
    }

    private HttpStatus manejarRespuestaDelServicio(Long idDataLog, ClienteWoowup cw, HttpStatusCode codeResponse, Cliente u) {
        switch (codeResponse) {
            case HttpStatus.OK:
                log.debug("[ VAR ] Cliente existe ? {}", true);
                if (woowUp2Service.actualizaCliente(cw, u.getKeyWoowup())) {
                    cerrarUnDataLog(idDataLog, "OK", "", "", CLIENTE_EXISTE);
                    return HttpStatus.OK;
                }
                break;
            case HttpStatus.NOT_FOUND:
                log.debug("[ VAR ] Cliente existe ? {}", false);
                if (woowUp2Service.creaCliente(cw, u.getKeyWoowup())) {
                    cerrarUnDataLog(idDataLog, "OK", "", "", CLIENTE_NUEVO);
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
        if (redisService.existeCorreo("CORREO_".concat(email))) {
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
            redisService.insertSku("CORREO_".concat(email), result);
            return true;
        }else{
            log.debug("[ NOK ] El email {} NO es vàlido ", email);
            return false;
        }

    }

    private boolean esTipoPermitidoDeDocto(String jsonBsale ) {
        log.debug("[ PROCESS ] esTipoPermitidoDeDocto");
        Gson gson = new Gson();
        BsaleResponse bsaleResponse = gson.fromJson(jsonBsale, BsaleResponse.class);

        return null != bsaleResponse &&
                (bsaleResponse.getDocumentType().getUse() == 0 ||
                 bsaleResponse.getDocumentType().getUse() == 1
                ) ;
    }

    private Long crearDataLog(Senal senal, String idCliente) {
        Log l = new Log();
        l.setIdCliente(idCliente);
        l.setIdAplicativo("INT-BSALE-WOOWUP");
        l.setServer(getIpServer());
        l.setSistemaOrigen("BSALE");
        l.setFechaIngreso(new Date());
        l.setDataOrigen( new Gson().toJson(senal) );
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
    private void cerrarUnDataLog(Long idDataLog, String resultado, String obs, String dataDestino, int clienteNuevo){
        log.debug("[ PROCESS ] Cerrando data-log con id: {}" , idDataLog);
        Log l = logRepository.getByIdDeLog(idDataLog);
        l.setFechaDestino(new Date());
        l.setResultado(resultado);
        l.setObservacion(obs);
        l.setDataDestino(dataDestino);
        l.setClienteNew(String.valueOf( clienteNuevo ));
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

}
