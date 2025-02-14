package cl.integrador.bsale.woowup.model.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Senal {

    //{"cpnId":11420,"resource":"/documents/1811905.json","resourceId":"1811905","topic":"document","action":"post","officeId":"11","send":1737740416}
    private Long cpnId;

    private String resource;

    private String resourceId;

    private String topic;

    private String action;

    private String officeId;

    private Long send;

    @Override
    public String toString() {
        return "Senal{" +
                "cpnId=" + cpnId +
                ", resource='" + resource + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", topic='" + topic + '\'' +
                ", action='" + action + '\'' +
                ", officeId='" + officeId + '\'' +
                ", send=" + send +
                '}';
    }
}
