// med/voll/api/consulta/ConsultaRepository.java
package med.voll.api.consulta;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    Page<Consulta> findAllByMedicoIdOrderByDataHoraDesc(Long medicoId, Pageable p);
    Page<Consulta> findAllByPacienteIdOrderByDataHoraDesc(Long pacienteId, Pageable p);
    boolean existsByMedicoIdAndDataHoraAndStatus(Long medicoId, LocalDateTime dh, Consulta.Status status);
    boolean existsByPacienteIdAndDataHoraAndStatus(Long pacienteId, LocalDateTime dh, Consulta.Status status);
}
