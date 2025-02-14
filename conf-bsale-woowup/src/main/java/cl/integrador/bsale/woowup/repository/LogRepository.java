package cl.integrador.bsale.woowup.repository;

import cl.integrador.bsale.woowup.model.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {

    @Query(value = "SELECT id, cliente, aplicativo, server, sistema_origen, fecha_ingreso, " +
            " data_origen, sistema_destino, fecha_destino, resultado, observacion, data_destino\n" +
            " FROM integrador_log  "
            + " WHERE id = ?1  ", nativeQuery = true)
    public Log getByIdDeLog(Long idLog);


}
