package cl.integrador.bsale.woowup.repository;

import cl.integrador.bsale.woowup.model.entity.Mail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MailDataRepository extends JpaRepository<Mail, Long> {



}
