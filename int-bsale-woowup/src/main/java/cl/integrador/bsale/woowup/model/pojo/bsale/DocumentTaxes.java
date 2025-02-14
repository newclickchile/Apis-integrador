
package cl.integrador.bsale.woowup.model.pojo.bsale;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DocumentTaxes {

    @SerializedName("href")
    @Expose
    public String href;

}
