// med/voll/api/consulta/Consulta.java
package med.voll.api.consulta;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import med.voll.api.medico.Medico;
import med.voll.api.paciente.Paciente;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultas",
        uniqueConstraints = @UniqueConstraint(name="uk_consulta_medico_horario",
                columnNames = {"medico_id","data_hora"}))
@Getter
@NoArgsConstructor
public class Consulta {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name="medico_id")
    private Medico medico;

    @ManyToOne(optional = false) @JoinColumn(name="paciente_id")
    private Paciente paciente;

    @Column(name="data_hora", nullable=false)
    private LocalDateTime dataHora;

    @Column(length = 500)
    private String observacoes;

    @Enumerated(EnumType.STRING)
    private Status status = Status.AGENDADA;

    public enum Status { AGENDADA, CANCELADA }

    public Long getId() {
        return id;
    }

    public Medico getMedico() {
        return medico;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public Status getStatus() {
        return status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public Consulta(Medico m, Paciente p, LocalDateTime quando, String obs){
        this.medico = m; this.paciente = p; this.dataHora = quando; this.observacoes = obs;
        this.status = Status.AGENDADA;
    }

    public void cancelar(){ this.status = Status.CANCELADA; }
}
