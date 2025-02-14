package cl.integrador.bsale.woowup.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "integrador", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;

//    @Column(columnDefinition = "JSONB")
//    private String metadata;

//    @CreationTimestamp
//    private LocalDateTime createdAt;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    public Long id;

    @Column(name="cliente", nullable = false)
    private String idCliente;

    @Column(name="access_key", nullable = true)
    private String  accessKey;

    @Column(name="key_bsale", nullable = true)
    private String  keyBsale;

    @Column(name="key_woowup", nullable = true)
    private String  keyWoowup;

    @Column(name="habilitado", nullable = true)
    private boolean habilitado;

    @Column(name="office_web", nullable = true)
    private String officeWeb;

    @Column(name="email_validate", nullable = true)
    private boolean emailValidate;

    @Column(name="pais", nullable = true)
    private String pais;

}
