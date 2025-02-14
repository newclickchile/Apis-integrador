package cl.integrador.bsale.woowup.model.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Senal {

    private String clientId;

    private String secretKeyBsale;

    private String keyWoowup;

    private String codigoPais;

    private String officeWeb;

    private boolean emailValidate;

    private List<Local> offices;

}
