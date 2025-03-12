package cl.integrador.alegra.woowup.util;

import cl.integrador.alegra.woowup.model.pojo.ClienteWoowup;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WooeUpHelper {
//    public static String getJsonDeLaVenta(VentaWoowup v) {
//        String jsonPut = "{" +
//                "  \"document\": \"" + v.getDocument() + "\"," +
//                "  \"invoice_number\": \""+ v.getInvoice_number() + "\"," +
//                "  \"channel\": \""+ v.getChannel()+ "\"," +
//                "  \"purchase_detail\": [" ;
//        ArrayList<VentaWoowup.PurchaseDetail> ventas = v.getPurchase_detail();
//        for(VentaWoowup.PurchaseDetail p: ventas) {
//            jsonPut +="    {" +
//                    "      \"sku\": \"" + p.getSku() + "\"," +
//                    "      \"product_name\": \"" + p.getProduct_name() + "\"," +
//                    "      \"quantity\": " + p.getQuantity() + "," +
//                    "      \"unit_price\": "  + p.getUnit_price() + "," +
//                    "      \"brand\": \"" + p.getBrand() + "\"" +
//                    "    }, ";
//        }
//        if(ventas.size() > 0){
//            jsonPut = jsonPut.substring(0, jsonPut.length()-1);
//        }
//        jsonPut +="  ]," +
//                "  \"prices\": {" +
//                "    \"gross\": "+ v.getPrices().getGross()+ "," +
//                "    \"tax\": "+ v.getPrices().getTax()+ "," +
//                "    \"total\": " + v.getPrices().getTotal() +
//                "  }," +
//                "  \"branch_name\": \"" + v.getBranch_name() + "\"," +
////                "  \"seller\":{" +
////                "    \"name\": \"" + v.getSeller().getName() + "\"" +
////                "  }," +
//                "  \"createtime\": \"" + v.getCreatetime() + "\"," +
//                "  \"approvedtime\": \"" + v.getApprovedtime() + "\"" +
//                "}";
//        return jsonPut;
//    }

    public static String getJsonDatosCliente(ClienteWoowup cw) {
        String res = "{\"document\": \"" + cw.getDocument() + "\",";
        if(cw.getEmail().trim().length()> 0){
            res+= "\"email\": \""+cw.getEmail()+"\",";
        }
        if(cw.getFirstName().trim().length() == 0 && cw.getLastName().trim().length() == 0){
            res += "\"first_name\": \"" + cw.getCompany().replace("\"", "")
                    .replace("\t", "") + "\"," +
                    "\"last_name\": \"\" ," ;
        }else {
            res += "\"first_name\": \"" +
                    cw.getFirstName().replace("\"", "")
                            .replace("\t", "") + "\"," +
                    "\"last_name\": \"" + cw.getLastName().replace("\"", "")
                    .replace("\t", "") + "\"," ;
        }
        String st = cw.getStreet().trim().length() > 100? cw.getStreet().trim().substring(0,99): cw.getStreet().trim();
        res +=
                "\"street\": \""+ st +"\"," +
                "\"telephone\": \""+cw.getPhone()+"\"," +
                "\"state\": \""+cw.getState()+"\"," +
                "\"city\": \"" +cw.getCity()+ "\"," +
                "\"country\":\"" + cw.getCountry()+ "\", " +
                "\"postcode\":\"" + cw.getPostcode()+ "\"  " +
                "}";
        return res;
    }
}
