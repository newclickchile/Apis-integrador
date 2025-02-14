package cl.integrador.bsale.woowup.repository;

import cl.integrador.bsale.woowup.model.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDataRepository extends JpaRepository<Cliente, Long> {

    @Query(value = "SELECT id, cliente, access_key, key_bsale, key_woowup, habilitado, office_web, pais, email_validate " +
              " FROM integrador "
            + " WHERE cliente = ?1 and access_key = ?2", nativeQuery = true)
    public Cliente findByClientAndAccess(String idCliente, String accessKey);

}
