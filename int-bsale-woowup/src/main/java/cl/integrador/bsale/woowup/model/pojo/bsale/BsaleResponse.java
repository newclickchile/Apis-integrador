
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
public class BsaleResponse {

    @SerializedName("href")
    @Expose
    public String href;
    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("emissionDate")
    @Expose
    public Integer emissionDate;
    @SerializedName("expirationDate")
    @Expose
    public Integer expirationDate;
    @SerializedName("generationDate")
    @Expose
    public Integer generationDate;
    @SerializedName("rcofDate")
    @Expose
    public Integer rcofDate;
    @SerializedName("number")
    @Expose
    public Integer number;
    @SerializedName("serialNumber")
    @Expose
    public Object serialNumber;
    @SerializedName("trackingNumber")
    @Expose
    public String trackingNumber;
    @SerializedName("totalAmount")
    @Expose
    public Integer totalAmount;
    @SerializedName("netAmount")
    @Expose
    public Integer netAmount;
    @SerializedName("taxAmount")
    @Expose
    public Integer taxAmount;
    @SerializedName("exemptAmount")
    @Expose
    public Integer exemptAmount;
    @SerializedName("notExemptAmount")
    @Expose
    public Integer notExemptAmount;
    @SerializedName("exportTotalAmount")
    @Expose
    public Integer exportTotalAmount;
    @SerializedName("exportNetAmount")
    @Expose
    public Integer exportNetAmount;
    @SerializedName("exportTaxAmount")
    @Expose
    public Integer exportTaxAmount;
    @SerializedName("exportExemptAmount")
    @Expose
    public Integer exportExemptAmount;
    @SerializedName("commissionRate")
    @Expose
    public Integer commissionRate;
    @SerializedName("commissionNetAmount")
    @Expose
    public Integer commissionNetAmount;
    @SerializedName("commissionTaxAmount")
    @Expose
    public Integer commissionTaxAmount;
    @SerializedName("commissionTotalAmount")
    @Expose
    public Integer commissionTotalAmount;
    @SerializedName("percentageTaxWithheld")
    @Expose
    public Double percentageTaxWithheld;
    @SerializedName("purchaseTaxAmount")
    @Expose
    public Integer purchaseTaxAmount;
    @SerializedName("purchaseTotalAmount")
    @Expose
    public Integer purchaseTotalAmount;
    @SerializedName("address")
    @Expose
    public String address;
    @SerializedName("municipality")
    @Expose
    public String municipality;
    @SerializedName("city")
    @Expose
    public String city;
    @SerializedName("urlTimbre")
    @Expose
    public String urlTimbre;
    @SerializedName("urlPublicView")
    @Expose
    public String urlPublicView;
    @SerializedName("urlPdf")
    @Expose
    public String urlPdf;
    @SerializedName("urlPublicViewOriginal")
    @Expose
    public String urlPublicViewOriginal;
    @SerializedName("urlPdfOriginal")
    @Expose
    public String urlPdfOriginal;
    @SerializedName("token")
    @Expose
    public String token;
    @SerializedName("state")
    @Expose
    public Integer state;
    @SerializedName("commercialState")
    @Expose
    public Integer commercialState;
    @SerializedName("cancellationStatus")
    @Expose
    public Integer cancellationStatus;
    @SerializedName("cancellationDate")
    @Expose
    public Object cancellationDate;
    @SerializedName("urlXml")
    @Expose
    public String urlXml;
    @SerializedName("ted")
    @Expose
    public String ted;
    @SerializedName("salesId")
    @Expose
    public Object salesId;
    @SerializedName("informedSii")
    @Expose
    public Integer informedSii;
    @SerializedName("responseMsgSii")
    @Expose
    public String responseMsgSii;
    @SerializedName("document_type")
    @Expose
    public DocumentType documentType;
    @SerializedName("client")
    @Expose
    public Client client;
    @SerializedName("office")
    @Expose
    public Office office;
    @SerializedName("user")
    @Expose
    public User user;
    @SerializedName("coin")
    @Expose
    public Coin coin;
    @SerializedName("priceList")
    @Expose
    public PriceList priceList;
    @SerializedName("references")
    @Expose
    public References references;
    @SerializedName("document_taxes")
    @Expose
    public DocumentTaxes documentTaxes;
    @SerializedName("details")
    @Expose
    public Details details;
    @SerializedName("sellers")
    @Expose
    public Sellers sellers;
    @SerializedName("attributes")
    @Expose
    public Attributes__1 attributes;
    @SerializedName("payments")
    @Expose
    public List<Payment> payments;

    @Override
    public String toString() {
        return "BsaleResponse{" +
                "href='" + href + '\'' +
                ", id=" + id +
                ", emissionDate=" + emissionDate +
                ", expirationDate=" + expirationDate +
                ", generationDate=" + generationDate +
                ", rcofDate=" + rcofDate +
                ", number=" + number +
                ", serialNumber=" + serialNumber +
                ", trackingNumber='" + trackingNumber + '\'' +
                ", totalAmount=" + totalAmount +
                ", netAmount=" + netAmount +
                ", taxAmount=" + taxAmount +
                ", exemptAmount=" + exemptAmount +
                ", notExemptAmount=" + notExemptAmount +
                ", exportTotalAmount=" + exportTotalAmount +
                ", exportNetAmount=" + exportNetAmount +
                ", exportTaxAmount=" + exportTaxAmount +
                ", exportExemptAmount=" + exportExemptAmount +
                ", commissionRate=" + commissionRate +
                ", commissionNetAmount=" + commissionNetAmount +
                ", commissionTaxAmount=" + commissionTaxAmount +
                ", commissionTotalAmount=" + commissionTotalAmount +
                ", percentageTaxWithheld=" + percentageTaxWithheld +
                ", purchaseTaxAmount=" + purchaseTaxAmount +
                ", purchaseTotalAmount=" + purchaseTotalAmount +
                ", address='" + address + '\'' +
                ", municipality='" + municipality + '\'' +
                ", city='" + city + '\'' +
                ", urlTimbre='" + urlTimbre + '\'' +
                ", urlPublicView='" + urlPublicView + '\'' +
                ", urlPdf='" + urlPdf + '\'' +
                ", urlPublicViewOriginal='" + urlPublicViewOriginal + '\'' +
                ", urlPdfOriginal='" + urlPdfOriginal + '\'' +
                ", token='" + token + '\'' +
                ", state=" + state +
                ", commercialState=" + commercialState +
                ", cancellationStatus=" + cancellationStatus +
                ", cancellationDate=" + cancellationDate +
                ", urlXml='" + urlXml + '\'' +
                ", ted='" + ted + '\'' +
                ", salesId=" + salesId +
                ", informedSii=" + informedSii +
                ", responseMsgSii='" + responseMsgSii + '\'' +
                ", documentType=" + documentType +
                ", client=" + client +
                ", office=" + office +
                ", user=" + user +
                ", coin=" + coin +
                ", priceList=" + priceList +
                ", references=" + references +
                ", documentTaxes=" + documentTaxes +
                ", details=" + details +
                ", sellers=" + sellers +
                ", attributes=" + attributes +
                ", payments=" + payments +
                '}';
    }
}
