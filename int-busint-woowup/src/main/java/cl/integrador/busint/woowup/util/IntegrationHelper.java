package cl.integrador.busint.woowup.util;

import cl.integrador.busint.woowup.model.entity.Cliente;
import cl.integrador.busint.woowup.model.pojo.ClienteWoowup;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IntegrationHelper {
    private IntegrationHelper() {
       // nothing
    }




    public static ClienteWoowup getObjectClientWoowup(String jsonEvent, Cliente u, boolean emailValido) {
        log.debug("[ PROCESS ] getObjectClientWoowup " );
        JsonObject jsonObject = JsonParser.parseString(jsonEvent).getAsJsonObject();
        if(null == jsonObject.getAsJsonObject("message").get("client") ){
            log.warn("[ WARN ] [ Se ignora información, No viene el nodo CLIENT]" );
            return null;
        }
        String email = jsonObject
                .getAsJsonObject("message")
                .getAsJsonObject("client")
                .get("email")
                .getAsString();

        if(null == email || email.trim().length() == 0){
            log.warn("[ WARN ] [ Se ignora información, No viene el nodo EMAIL]" );
            return null;
        }
        String id = jsonObject
                .getAsJsonObject("message")
                .getAsJsonObject("client")
                .get("identification")
                .getAsString();
        if(null == id  || id.trim().length() == 0){
            log.warn("[ WARN ] [ Se ignora información, No viene el ID del Cliente ]" );
            return null;
        }

        ClienteWoowup cw = new ClienteWoowup();
        cw.setDocument( id.replaceAll("[-.]","").toUpperCase() );
        cw.setStreet( jsonObject
                .getAsJsonObject("message")
                .getAsJsonObject("client")
                .getAsJsonObject("address")
                .get("address")
                .getAsString() );
        cw.setState( jsonObject
                .getAsJsonObject("message")
                .getAsJsonObject("client")
                .getAsJsonObject("address")
                .get("province")
                .getAsString());
        cw.setCity( jsonObject
                .getAsJsonObject("message")
                .getAsJsonObject("client")
                .getAsJsonObject("address")
                .get("city")
                .getAsString() );
        if(emailValido) {
            cw.setEmail(email);
        }else{
            cw.setEmail("");
        }
        cw.setCountry( u.getPais() );
        cw.setFirstName( jsonObject
                .getAsJsonObject("message")
                .getAsJsonObject("client")
                .getAsJsonObject("name")
                .get("fullname")
                .getAsString());
        cw.setLastName( "");
        cw.setCompany( "");
        cw.setPoints( 0.0);
        cw.setPhone( jsonObject
                .getAsJsonObject("message")
                .getAsJsonObject("client")
                .get("phonePrimary")
                .getAsString());
        cw.setPostcode( jsonObject
                .getAsJsonObject("message")
                .getAsJsonObject("client")
                .getAsJsonObject("address")
                .get("postalCode")
                .getAsString());
        cw.setService_uid(String.valueOf( jsonObject
                .getAsJsonObject("message")
                .getAsJsonObject("client")
                .get("id")
                .getAsInt()));


        return cw;
    }

}
