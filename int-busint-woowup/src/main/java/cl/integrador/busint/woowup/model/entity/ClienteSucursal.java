package cl.integrador.busint.woowup.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "integrador_sucursal", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteSucursal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    public Long id;


    @Column(name="cliente", nullable = false)
    private String idCliente;

     @Column(name="id_sucursal", nullable = false)
    private String idSucursal;


    @Column(name="habilitado", nullable = true)
    private boolean habilitado;



}
