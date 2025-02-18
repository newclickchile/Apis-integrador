package cl.integrador.bsale.woowup.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "mailing_log", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    public Long id;

    @Column(name="cliente", nullable = true)
    private String idCliente;

    @Column(name="server", nullable = true)
    private String  server;

    @Column(name="mail", nullable = true)
    private String  mail;

    @Column(name="fecha_ingreso", nullable = true)
    private Date fechaIngreso;

    @Column(name="resultado", nullable = true)
    private String resultado;

    @Column(name="observacion", nullable = true)
    private String observacion;

    @Column(name="data_salida", nullable = true)
    private String dataSalida;

}
