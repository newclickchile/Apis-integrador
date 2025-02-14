
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
public class Client {

    @SerializedName("href")
    @Expose
    public String href;
    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("firstName")
    @Expose
    public String firstName;
    @SerializedName("lastName")
    @Expose
    public String lastName;
    @SerializedName("email")
    @Expose
    public String email;
    @SerializedName("code")
    @Expose
    public String code;
    @SerializedName("phone")
    @Expose
    public String phone;
    @SerializedName("company")
    @Expose
    public String company;
    @SerializedName("note")
    @Expose
    public String note;
    @SerializedName("facebook")
    @Expose
    public Object facebook;
    @SerializedName("twitter")
    @Expose
    public String twitter;
    @SerializedName("hasCredit")
    @Expose
    public Integer hasCredit;
    @SerializedName("maxCredit")
    @Expose
    public Double maxCredit;
    @SerializedName("state")
    @Expose
    public Integer state;
    @SerializedName("activity")
    @Expose
    public String activity;
    @SerializedName("city")
    @Expose
    public String city;
    @SerializedName("commerciallyBlocked")
    @Expose
    public Integer commerciallyBlocked;
    @SerializedName("municipality")
    @Expose
    public String municipality;
    @SerializedName("address")
    @Expose
    public String address;
    @SerializedName("companyOrPerson")
    @Expose
    public Integer companyOrPerson;
    @SerializedName("accumulatePoints")
    @Expose
    public Integer accumulatePoints;
    @SerializedName("points")
    @Expose
    public Double points;
    @SerializedName("pointsUpdated")
    @Expose
    public Object pointsUpdated;
    @SerializedName("sendDte")
    @Expose
    public Integer sendDte;
    @SerializedName("isForeigner")
    @Expose
    public Integer isForeigner;
    @SerializedName("prestashopClienId")
    @Expose
    public Integer prestashopClienId;
    @SerializedName("createdAt")
    @Expose
    public Integer createdAt;
    @SerializedName("updatedAt")
    @Expose
    public Integer updatedAt;
    @SerializedName("contacts")
    @Expose
    public Contacts contacts;
    @SerializedName("attributes")
    @Expose
    public Attributes attributes;
    @SerializedName("addresses")
    @Expose
    public Addresses addresses;

}
