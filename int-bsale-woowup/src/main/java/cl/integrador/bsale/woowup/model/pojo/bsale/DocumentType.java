
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
public class DocumentType {

    @SerializedName("href")
    @Expose
    public String href;
    @SerializedName("id")
    @Expose
    public Integer id;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("initialNumber")
    @Expose
    public Integer initialNumber;
    @SerializedName("codeSii")
    @Expose
    public String codeSii;
    @SerializedName("isElectronicDocument")
    @Expose
    public Integer isElectronicDocument;
    @SerializedName("breakdownTax")
    @Expose
    public Integer breakdownTax;
    @SerializedName("use")
    @Expose
    public Integer use;
    @SerializedName("isSalesNote")
    @Expose
    public Integer isSalesNote;
    @SerializedName("isExempt")
    @Expose
    public Integer isExempt;
    @SerializedName("restrictsTax")
    @Expose
    public Integer restrictsTax;
    @SerializedName("useClient")
    @Expose
    public Integer useClient;
    @SerializedName("messageBodyFormat")
    @Expose
    public Object messageBodyFormat;
    @SerializedName("thermalPrinter")
    @Expose
    public Integer thermalPrinter;
    @SerializedName("state")
    @Expose
    public Integer state;
    @SerializedName("copyNumber")
    @Expose
    public Integer copyNumber;
    @SerializedName("isCreditNote")
    @Expose
    public Integer isCreditNote;
    @SerializedName("continuedHigh")
    @Expose
    public Integer continuedHigh;
    @SerializedName("ledgerAccount")
    @Expose
    public String ledgerAccount;
    @SerializedName("ipadPrint")
    @Expose
    public Integer ipadPrint;
    @SerializedName("ipadPrintHigh")
    @Expose
    public Integer ipadPrintHigh;
    @SerializedName("restrictClientType")
    @Expose
    public Integer restrictClientType;
    @SerializedName("useMaxDays")
    @Expose
    public Integer useMaxDays;
    @SerializedName("maxDays")
    @Expose
    public Integer maxDays;
    @SerializedName("book_type")
    @Expose
    public BookType bookType;

}
