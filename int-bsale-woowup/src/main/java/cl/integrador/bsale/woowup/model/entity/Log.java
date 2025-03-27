package cl.integrador.bsale.woowup.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "integrador_log", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    public Long id;

    @Column(name="cliente", nullable = true)
    private String idCliente;

    @Column(name="aplicativo", nullable = true)
    private String idAplicativo;

    @Column(name="server", nullable = true)
    private String  server;

    @Column(name="sistema_origen", nullable = true)
    private String  sistemaOrigen;

    @Column(name="fecha_ingreso", nullable = true)
    private Date fechaIngreso;

    @Column(name="data_origen", nullable = true)
    private String dataOrigen;

    @Column(name="sistema_destino", nullable = true)
    private String sistemaDestino;

    @Column(name="fecha_destino", nullable = true)
    private Date fechaDestino;

    @Column(name="resultado", nullable = true)
    private String resultado;

    @Column(name="observacion", nullable = true)
    private String observacion;

    @Column(name="data_destino", nullable = true)
    private String dataDestino;

    @Column(name="cliente_new", nullable = true)
    private String clienteNew;



}
