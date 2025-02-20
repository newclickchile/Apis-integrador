package cl.integrador.bsale.woowup.util;

import cl.integrador.bsale.woowup.model.entity.Cliente;
import cl.integrador.bsale.woowup.model.pojo.ClienteWoowup;
import cl.integrador.bsale.woowup.model.pojo.VentaWoowup;
import cl.integrador.bsale.woowup.model.pojo.bsale.BsaleResponse;
import cl.integrador.bsale.woowup.model.pojo.bsale.Item;
import cl.integrador.bsale.woowup.model.pojo.bsale.Item__2;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class IntegrationHelper {
//    public static VentaWoowup getObjectVentaWoowump(String resFromBase) {
//        log.debug("[ PROCESS ] getObjectClientWoowup: {}", resFromBase);
//        JsonObject orden = new JsonParser().parse(resFromBase).getAsJsonObject();
//        VentaWoowup vw = new VentaWoowup();
//        vw.setDocument( orden.get("client").getAsJsonObject().get("code").getAsString().replaceAll("-","").replaceAll(".","").toUpperCase() );
//        vw.setInvoiceNumber( orden.get("number").getAsString() );
//        vw.setBranchName( orden.get("office").getAsJsonObject().get("name").getAsString() );
//        vw.setUseType( orden.get("document_type").getAsJsonObject().get("use").getAsString() );
//
//        VentaWoowup.Prices prices = new VentaWoowup.Prices();
//        prices.setGross( orden.get("netAmount'").getAsDouble() );
//        prices.setTax(orden.get("taxAmount''").getAsDouble());
//        prices.setTotal(orden.get("totalAmount''").getAsDouble());
//        vw.setPrices( prices);
//
//        String vendedor = "";
//        JsonArray atributos = orden.get("attributes").getAsJsonObject().get("items").getAsJsonArray();
//        for (JsonElement elemento : atributos) {
//            JsonObject atributo = elemento.getAsJsonObject();
//            String nombre = atributo.get("name").getAsString();
//            if(nombre.equalsIgnoreCase("Vendedor")){
//                JsonElement d = atributo.get("attributes").getAsJsonArray().get(0);
//                JsonObject da = elemento.getAsJsonObject();
//                vendedor = da.get("name").getAsString();
//            }
//        }
//        VentaWoowup.Seller seller = new VentaWoowup.Seller();
//        seller.setName(vendedor);
//        vw.setSeller(seller);
//
//        return null;
//    }

    public static VentaWoowup getObjectVentaWoowump(BsaleResponse b) {
        log.debug("[ PROCESS ] getObjectClientWoowup " );
        VentaWoowup vw = new VentaWoowup();
        vw.setDocument( b.getClient().getCode().replaceAll("[-.]","").toUpperCase() );
        vw.setInvoice_number( b.getNumber().toString()  );
        vw.setChannel("in-store");
        vw.setBranch_name( b.getOffice().getName() );
        //vw.setUseType( b.getDocumentType().getUse().toString() );

        VentaWoowup.Prices prices = new VentaWoowup.Prices();
        prices.setGross( b.getNetAmount()  );
        prices.setTax(b.getTaxAmount());
        prices.setTotal(b.getTotalAmount());
        vw.setPrices( prices);

        String vendedor = "";
        if(null != b.getSellers().getItems()) {
            List<Item__2> items = b.getSellers().getItems();
            if (items.size() > 0) {
                Item__2 d = items.get(0);
                vendedor = d.getFirstName().concat(" ").concat(d.getLastName());
            }
            if (null != vendedor && vendedor.trim().length() > 0) {
                VentaWoowup.Seller seller = new VentaWoowup.Seller();
                seller.setName(vendedor);
                seller.setEmail("noreply@sininfo.com");
                vw.setSeller(seller);
            }
        }
        ArrayList<VentaWoowup.PurchaseDetail> listpd = new ArrayList<>();
        for (Item d: b.getDetails().getItems()){
            VentaWoowup.PurchaseDetail pd = new VentaWoowup.PurchaseDetail();
            pd.setSku(d.getVariant().getCode());
            pd.setProduct_name(d.getVariant().getDescription()
                    .replace("\"", "")
                    .replace("\t", ""));
            pd.setUnit_price(d.getNetAmount());
            pd.setQuantity(d.getQuantity());
            pd.setBrand(d.getVariant().getHref());
            listpd.add(pd);
        }
        vw.setPurchase_detail(listpd);

        vw.setCreatetime(formatTimestamp(b.getGenerationDate()) );
        vw.setApprovedtime(formatTimestamp(b.getGenerationDate()) );
        vw.setPoints(b.getClient().getPoints());
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



    //    public static ClienteWoowup getObjectClientWoowup(String resFromBase) {
//        log.debug("[ PROCESS ] getObjectClientWoowup: {}", resFromBase);
//        JsonObject orden = new JsonParser().parse(resFromBase).getAsJsonObject();
//        ClienteWoowup cw = new ClienteWoowup();
//        cw.setDocument( orden.get("client").getAsJsonObject().get("code").getAsString().replaceAll("-","").replaceAll(".","").toUpperCase() );
//        cw.setStreet( orden.get("client").getAsJsonObject().get("address").getAsString() );
//        cw.setState( orden.get("client").getAsJsonObject().get("municipality").getAsString() );
//        cw.setCity( orden.get("client").getAsJsonObject().get("city").getAsString() );
//        cw.setEmail( orden.get("client").getAsJsonObject().get("email").getAsString() );
//        cw.setEmail( orden.get("client").getAsJsonObject().get("email").getAsString() );
//        cw.setCountry( "CHL" );
//
//        return cw;
//    }
    public static ClienteWoowup getObjectClientWoowup(BsaleResponse b, Cliente u, boolean emailValido) {
        log.debug("[ PROCESS ] getObjectClientWoowup " );
        if(null == b.getClient()){
            log.warn("[ WARN ] [ No viene el nodo CLIENT]" );
            return null;
        }
        if(null == b.getClient().getCode()){
            log.warn("[ WARN ] [ No viene el nodo CODE]" );
            return null;
        }
        if(null == b.getClient().getEmail()){
            log.warn("[ WARN ] [ No viene el nodo EMAIL]" );
            return null;
        }

        ClienteWoowup cw = new ClienteWoowup();
        cw.setDocument( b.getClient().getCode().replaceAll("[-.]","").toUpperCase() );
        cw.setStreet( b.getClient().getAddress() );
        cw.setState( b.getClient().getMunicipality() );
        cw.setCity( b.getClient().getCity() );
        if(emailValido) {
            cw.setEmail(b.getClient().getEmail());
        }else{
            cw.setEmail("");
        }
        cw.setCountry( u.getPais() );
        cw.setFirstName( b.getClient().getFirstName());
        cw.setLastName( b.getClient().getLastName());
        cw.setCompany( b.getClient().getCompany());
        cw.setPoints( b.getClient().getPoints() );
        cw.setPhone( b.getClient().getPhone());
        return cw;
    }

}
