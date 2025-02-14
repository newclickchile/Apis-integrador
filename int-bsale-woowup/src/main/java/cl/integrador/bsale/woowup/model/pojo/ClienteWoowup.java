package cl.integrador.bsale.woowup.model.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ClienteWoowup {

    private String document;

    private String email;

    private String firstName;

    private String lastName;

    private String street;

    private String state;

    private String city;

    private String country;

    private String company;

    private String phone;

    private Double points;

    @Override
    public String toString() {
        return "ClienteWoowup{" +
                "document='" + document + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", street='" + street + '\'' +
                ", state='" + state + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", company='" + company + '\'' +
                ", phone='" + phone + '\'' +
                ", points=" + points +
                '}';
    }
}
