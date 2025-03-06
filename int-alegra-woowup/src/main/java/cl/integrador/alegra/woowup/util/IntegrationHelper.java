package cl.integrador.alegra.woowup.util;

import cl.integrador.alegra.woowup.model.entity.Cliente;
import cl.integrador.alegra.woowup.model.pojo.ClienteWoowup;
import cl.integrador.alegra.woowup.model.pojo.VentaWoowup;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class IntegrationHelper {
    private IntegrationHelper() {
       // nothing
    }

    public static VentaWoowup getObjectVentaWoowump(String jsonAlegra  ) {
        log.debug("[ PROCESS ] getObjectClientWoowup " );
        float factor =  1;
        JsonObject jsonObject = JsonParser.parseString(jsonAlegra).getAsJsonObject();

        VentaWoowup vw = new VentaWoowup();
        vw.setDocument( jsonObject
                .getAsJsonObject("message")
                .getAsJsonObject("client")
                .get("identification")
                .getAsString().replaceAll("[-.]","").toUpperCase() );
        vw.setInvoice_number(jsonObject
                .get("id")
                .getAsString() );
        vw.setChannel(  "in-store");
        vw.setBranch_name( jsonObject
                .getAsJsonObject("costCenter")
                .get("code")
                .getAsString() );

        VentaWoowup.Prices prices = new VentaWoowup.Prices();
        prices.setGross( 0 * factor);
        prices.setTax(0 * factor);
        prices.setTotal(jsonObject
                .get("total")
                .getAsFloat() * factor);
        vw.setPrices( prices);

        String vendedor = jsonObject
                .getAsJsonObject("seller")
                .get("name")
                .getAsString();

        if (null != vendedor && vendedor.trim().length() > 0) {
            VentaWoowup.Seller seller = new VentaWoowup.Seller();
            seller.setName(vendedor);
            seller.setEmail("noreply@sininfo.com");
            vw.setSeller(seller);
        }

        ArrayList<VentaWoowup.PurchaseDetail> listpd = new ArrayList<>();
        for (JsonElement itemElement : jsonObject.get("items").getAsJsonArray()) {
            JsonObject itemObject = itemElement.getAsJsonObject();

            VentaWoowup.PurchaseDetail pd = new VentaWoowup.PurchaseDetail();
            pd.setSku(  itemObject.get("reference").getAsString());
            pd.setProduct_name(itemObject.get("name").getAsString()
                    .replace("\"", "")
                    .replace("\t", ""));
            pd.setUnit_price(itemObject.get("price").getAsFloat() * factor);
            pd.setQuantity(itemObject.get("quantity").getAsDouble());
            pd.setBrand("");
            listpd.add(pd);
        }
        vw.setPurchase_detail(listpd);

        vw.setCreatetime( jsonObject
                 .get("datetime")
                .getAsString()  );
        vw.setApprovedtime(jsonObject
                .get("datetime")
                .getAsString()  );
        vw.setPoints( 0.0 );
        return vw;
    }

    public static String formatTimestamp(long timestamp) {
        // Convertir el timestamp a un objeto Instant
        Instant instant = Instant.ofEpochSecond(timestamp);

        // Convertir el Instant a ZonedDateTime en la zona horaria de Chile Summer Time
        ZonedDateTime zonedDateTime = instant.atZone(ZoneOffset.UTC);

        // Formatear ZonedDateTime en una cadena
        return zonedDateTime.toString();
    }


    public static ClienteWoowup getObjectClientWoowup(String jsonAlegra, Cliente u, boolean emailValido) {
        log.debug("[ PROCESS ] getObjectClientWoowup " );
        JsonObject jsonObject = JsonParser.parseString(jsonAlegra).getAsJsonObject();
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
