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
import org.apache.commons.lang3.StringEscapeUtils;
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

    @Value("${check.mail}")
    private String checkAllIncomingMail;

    @Async("asyncTaskExecutor")
    public void procesoAsyncDelEvento(Senal senal, Cliente clienteBD, String idCliente, String accessKey) {
        Long idDataLog = crearDataLog( senal,  clienteBD,  idCliente);
        log.debug("{}[ ================================ ]", idDataLog);
        log.debug("{}[ =   S T A R T      A S Y N C   = ]", idDataLog);
        log.debug("{}[ ================================ ]", idDataLog);
        log.debug("{}[ VAR ] Buscando los datos del cliente : {}" , idDataLog, idCliente);
        Gson gson = new Gson();
        ClienteSucursal s = userSucursalDataRepository.findByClientAndSucursal(idCliente, senal.getOfficeId(), true);
        if (null == s) {
            log.debug("{}[ VAR ] Llamando servicio Bsale : {}", idDataLog , senal.getResource().split("/")[2]);
            String jsonBsale = bsale2Service.getInfo(senal.getResource().split("/")[2], clienteBD.getKeyBsale());
            if (null != jsonBsale) {
                if(!esTipoPermitidoDeDocto(jsonBsale)){
                    BsaleResponse bsaleResponse = gson.fromJson(jsonBsale, BsaleResponse.class);
                    log.warn("{} Se ignora la informacion por que el tipo documento {} no esta permitido", idDataLog ,
                            bsaleResponse.getDocumentType().getUse());
                    cerrarUnDataLog(idDataLog, "NOK", "Se ignora la informacion por que el tipo documento no esta permitido",
                            "Tipo documento no permitida es : "+bsaleResponse.getDocumentType().getUse());
                }else{
                    int res = procesoCreacionActualizacionDecliente(jsonBsale, clienteBD);
                    if (res == HttpStatus.OK.value()) {
                        ingresarLaVenta(idDataLog, jsonBsale, clienteBD);
                    } else {
                        if (res == HttpStatus.FORBIDDEN.value()) {
                            log.warn("{} Se ignora informaciòn. Falta el nodo 'Client'" , idDataLog);
                            cerrarUnDataLog(idDataLog, "NOK", "Se ignora informaciòn. Falta el nodo 'Client'",
                                    "");
                        } else {

                            if (res == HttpStatus.BAD_REQUEST.value()) {
                                log.warn("{} Se ignora informaciòn. Correo NO es valido" , idDataLog);
                                cerrarUnDataLog(idDataLog, "NOK", "Se ignora informaciòn. Correo NO es valido",
                                        "");
                            } else {
                                log.error("{} }Error en el proceso de Crear/Actualizar cliente", idDataLog);
                                cerrarUnDataLog(idDataLog, "NOK", res + " Error en el proceso de Crear/Actualizar cliente",
                                        StringEscapeUtils.unescapeJava(gson.toJson(clienteBD)));
                            }
                        }
                    }
                }
            } else {
                log.error("{} Problemas al obtener info de BSALE de : {}", idDataLog, senal.getResource().split("/")[2]);
                cerrarUnDataLog(idDataLog, "NOK", "Error en el proceso de leer desde Bsale los datos del cliente",
                        StringEscapeUtils.unescapeJava( gson.toJson(clienteBD) ));
            }
        } else {
            log.warn("{} Se ignora la informacion por que la sucursal {} no esta autorizada", idDataLog, senal.getOfficeId());
            cerrarUnDataLog(idDataLog, "NOK", "Se ignora la informacion por que la sucursal no esta autorizada",
                   "La sucursal no permitida es : "+ senal.getOfficeId());
        }
        log.debug("{}[ ============================ ]", idDataLog);
        log.debug("{}[ =   E N D      A S Y N C   = ]", idDataLog);
        log.debug("{}[ ============================ ]", idDataLog);
    }



    private void ingresarLaVenta(Long idDataLog, String jsonBsale, Cliente u) {
        log.debug("{}[ PROCESS ] ingresarLaVenta: {}", idDataLog, u.getIdCliente());
        Gson gson = new Gson();
        BsaleResponse bsaleResponse = gson.fromJson(jsonBsale, BsaleResponse.class);
        VentaWoowup vw = IntegrationHelper.getObjectVentaWoowump(bsaleResponse);
//        log.debug("[ VAR ] VentaWoowup : {}" , new Gson().toJson(vw));
        ArrayList<VentaWoowup.PurchaseDetail> newlistPd = new ArrayList<>();
        if(null != vw){
            for(VentaWoowup.PurchaseDetail pd: vw.getPurchase_detail()) {
                String jsonProducto = bsale2Service.getProduct(pd.getBrand(), u.getKeyBsale());
                if (null != jsonProducto) {
                    JsonObject orden = new JsonParser().parse(jsonProducto).getAsJsonObject();
                    pd.setBrand(orden.get("product").getAsJsonObject().get("name")
                            .getAsString().replace("\"", "") );
                }
                newlistPd.add(pd);
            }
        }
        vw.setPurchase_detail(newlistPd);
        //log.debug("[ VAR ] VentaWoowup: {}", new Gson().toJson(vw));
        WoowupResponse httpCode = woowUp2Service.ingresarVenta(vw, u.getKeyWoowup());
        if(httpCode.getResultCode() == HttpStatus.OK.value()){
            cerrarUnDataLog(idDataLog, "OK",String.valueOf(HttpStatus.OK.value())
                    , StringEscapeUtils.unescapeJava( gson.toJson(vw) ));
        }else {
            cerrarUnDataLog(idDataLog, "NOK",httpCode.getResultCode() + " " + httpCode.getMessage(),
                    StringEscapeUtils.unescapeJava( gson.toJson(vw) ));
        }
    }

    private int procesoCreacionActualizacionDecliente(String jsonBsale, Cliente u) {
        Gson gson = new Gson();
        BsaleResponse bsaleResponse = gson.fromJson(jsonBsale, BsaleResponse.class);
        boolean emailValido = isEmailValidoDelCliente( bsaleResponse.getClient().getEmail(), u);
        if(!emailValido){
            log.warn("[ ATENCION ] el email {} no es valido segun servicio externo checkMail"
                    , bsaleResponse.getClient().getEmail() );
            return HttpStatus.BAD_REQUEST.value();
        }else {
            log.debug("[ PROCESS ] procesoCreacionActualizacionDecliente: {}", u.getIdCliente());
            ClienteWoowup cw = IntegrationHelper.getObjectClientWoowup(bsaleResponse, u);
            if (null != cw) {
                //            log.debug("[ VAR ] ClienteWoowup: {}", new Gson().toJson(cw));
                HttpStatusCode codeResponse = woowUp2Service.existeCliente(cw, u.getKeyWoowup());
                if (codeResponse == HttpStatus.OK) {
                    log.debug("[ VAR ] Cliente existe ? {}", true);
                    if (woowUp2Service.actualizaCliente(cw, u.getKeyWoowup())) {
                        return HttpStatus.OK.value();
                    }
                } else {
                    if (codeResponse == HttpStatus.NOT_FOUND) {
                        log.debug("[ VAR ] Cliente existe ? {}", false);
                        if (woowUp2Service.creaCliente(cw, u.getKeyWoowup())) {
                            return HttpStatus.OK.value();
                        }
                    }
                }
            } else {
                return HttpStatus.FORBIDDEN.value();
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    private boolean isEmailValidoDelCliente(String email, Cliente u) {
        if( !checkAllIncomingMail.equalsIgnoreCase("1")){ return true; }
        if( !u.isEmailValidate()){ return true; }

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

    private boolean esTipoPermitidoDeDocto(String jsonBsale ) {
        log.debug("[ PROCESS ] esTipoPermitidoDeDocto");
        Gson gson = new Gson();
        BsaleResponse bsaleResponse = gson.fromJson(jsonBsale, BsaleResponse.class);

        if(null != bsaleResponse &&
                (bsaleResponse.getDocumentType().getUse() == 0 ||
                 bsaleResponse.getDocumentType().getUse() == 1
                ) ){
            return true;
        }
       return false;
    }

    private Long crearDataLog(Senal senal, Cliente u, String idCliente) {
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
