package med.voll.api.consulta;

import java.time.LocalDateTime;
public record DadosConsultaLista(
        Long id, String medicoNome, String pacienteNome,
        LocalDateTime dataHora, String status) {}
