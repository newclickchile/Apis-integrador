package cl.integrador.busint.woowup.util;

import cl.integrador.busint.woowup.model.pojo.ClienteWoowup;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WooeUpHelper {
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
