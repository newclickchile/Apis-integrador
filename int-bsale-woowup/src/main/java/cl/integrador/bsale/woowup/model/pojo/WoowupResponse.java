package cl.integrador.bsale.woowup.model.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class WoowupResponse {

    private int resultCode;

    private String message;


}
