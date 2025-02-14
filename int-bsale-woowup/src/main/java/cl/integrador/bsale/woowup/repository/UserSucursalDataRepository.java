package cl.integrador.bsale.woowup.repository;

import cl.integrador.bsale.woowup.model.entity.ClienteSucursal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSucursalDataRepository extends JpaRepository<ClienteSucursal, Long> {


    @Query(value = "SELECT id, cliente, id_sucursal, habilitado" +
              " FROM integrador_sucursal "
            + " WHERE cliente = ?1 and id_sucursal = ?2 and habilitado = ?3", nativeQuery = true)
    public ClienteSucursal findByClientAndSucursal(String idCliente, String idSucursal, boolean habilitado);

}
