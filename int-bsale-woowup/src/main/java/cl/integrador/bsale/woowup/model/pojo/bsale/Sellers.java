
package cl.integrador.bsale.woowup.model.pojo.bsale;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Sellers {

    @SerializedName("href")
    @Expose
    public String href;
    @SerializedName("count")
    @Expose
    public Integer count;
    @SerializedName("limit")
    @Expose
    public Integer limit;
    @SerializedName("offset")
    @Expose
    public Integer offset;
    @SerializedName("items")
    @Expose
    public List<Item__2> items;

}
