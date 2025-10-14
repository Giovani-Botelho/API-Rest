package med.voll.api.consulta;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
public record DadosAgendarConsulta(
        Long medicoId,    // obrigatório se o token pertence a PACIENTE
        Long pacienteId,  // obrigatório se o token pertence a MEDICO
        @NotNull @Future LocalDateTime dataHora,
        String observacoes
) {}
