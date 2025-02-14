
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
public class Office {

    @SerializedName("href")
    @Expose
    public String href;
    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("description")
    @Expose
    public String description;
    @SerializedName("address")
    @Expose
    public String address;
    @SerializedName("latitude")
    @Expose
    public String latitude;
    @SerializedName("longitude")
    @Expose
    public String longitude;
    @SerializedName("isVirtual")
    @Expose
    public Integer isVirtual;
    @SerializedName("country")
    @Expose
    public String country;
    @SerializedName("municipality")
    @Expose
    public String municipality;
    @SerializedName("city")
    @Expose
    public String city;
    @SerializedName("zipCode")
    @Expose
    public String zipCode;
    @SerializedName("email")
    @Expose
    public String email;
    @SerializedName("costCenter")
    @Expose
    public String costCenter;
    @SerializedName("state")
    @Expose
    public Integer state;
    @SerializedName("imagestionCellarId")
    @Expose
    public Integer imagestionCellarId;
    @SerializedName("store")
    @Expose
    public Integer store;
    @SerializedName("defaultPriceList")
    @Expose
    public Integer defaultPriceList;

}
