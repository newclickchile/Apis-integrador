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


    public static VentaWoowup getObjectVentaWoowump(BsaleResponse b, Cliente u ) {
        log.debug("[ PROCESS ] getObjectClientWoowup " );
        int factor = b.getDocumentType().getUse() == 1?-1:1;
        VentaWoowup vw = new VentaWoowup();
        vw.setDocument( b.getClient().getCode().replaceAll("[-.]","").toUpperCase() );
        vw.setInvoice_number( b.getDocumentType().getId().toString().
                concat("_").
                concat(b.getNumber().toString() ) );
        vw.setChannel(b.getOffice().getId().equals(u.getOfficeWeb()) ? "web": "in-store");
        vw.setBranch_name( b.getOffice().getName() );
        //vw.setUseType( b.getDocumentType().getUse().toString() );

        VentaWoowup.Prices prices = new VentaWoowup.Prices();
        prices.setGross( b.getNetAmount() * factor );
        prices.setTax(b.getTaxAmount() * factor);
        prices.setTotal(b.getTotalAmount() * factor);
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
            pd.setUnit_price(d.getNetAmount() * factor);
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


    public static ClienteWoowup getObjectClientWoowup(BsaleResponse b, Cliente u, boolean emailValido) {
        log.debug("[ PROCESS ] getObjectClientWoowup " );
        if(null == b.getClient()){
            log.warn("[ WARN ] [ Se ignora información, No viene el nodo CLIENT]" );
            return null;
        }
        if(null == b.getClient().getEmail()  || b.getClient().getEmail().trim().length() == 0){
            log.warn("[ WARN ] [ Se ignora información, No viene el nodo EMAIL]" );
            return null;
        }
        if(null == b.getClient().getCode()  || b.getClient().getCode().trim().length() == 0){
            log.warn("[ WARN ] [ Se ignora información, No viene el ID del Cliente ]" );
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
