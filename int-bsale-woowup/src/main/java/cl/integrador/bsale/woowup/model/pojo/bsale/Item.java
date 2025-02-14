
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
public class Item {

    @SerializedName("href")
    @Expose
    public String href;
    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("lineNumber")
    @Expose
    public Integer lineNumber;
    @SerializedName("quantity")
    @Expose
    public Double quantity;
    @SerializedName("netUnitValue")
    @Expose
    public Integer netUnitValue;
    @SerializedName("netUnitValueRaw")
    @Expose
    public Double netUnitValueRaw;
    @SerializedName("totalUnitValue")
    @Expose
    public Integer totalUnitValue;
    @SerializedName("netAmount")
    @Expose
    public Integer netAmount;
    @SerializedName("taxAmount")
    @Expose
    public Integer taxAmount;
    @SerializedName("totalAmount")
    @Expose
    public Integer totalAmount;
    @SerializedName("netDiscount")
    @Expose
    public Integer netDiscount;
    @SerializedName("totalDiscount")
    @Expose
    public Integer totalDiscount;
    @SerializedName("variant")
    @Expose
    public Variant variant;
    @SerializedName("note")
    @Expose
    public String note;
    @SerializedName("relatedDetailId")
    @Expose
    public Integer relatedDetailId;

}
