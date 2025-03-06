package cl.integrador.alegra.woowup.model.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class VentaWoowup {

    public String document;
    public String invoice_number;
    public Double points;
    public String channel;
    public ArrayList<PurchaseDetail> purchase_detail ;
    public Prices prices;
    public String branch_name;
    public Seller seller;
    public String createtime;
    public String approvedtime;


    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Seller{
        public String name;
        public String email;
    }
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Prices{
        public double gross;
        public double tax;
        public double total;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    public static class PurchaseDetail{
        public String sku;
        public String product_name;
        public double quantity;
        public double unit_price;
        public String brand;
    }

    @Override
    public String toString() {
        return "VentaWoowup{" +
                "document='" + document + '\'' +
                ", invoice_number='" + invoice_number + '\'' +
                ", channel='" + channel + '\'' +
                ", purchase_detail=" + purchase_detail +
                ", prices=" + prices +
                ", branch_name='" + branch_name + '\'' +
                ", seller=" + seller +
                ", createtime='" + createtime + '\'' +
                ", approvedtime='" + approvedtime + '\'' +
                '}';
    }
}
