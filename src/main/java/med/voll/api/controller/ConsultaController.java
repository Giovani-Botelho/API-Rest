// med/voll/api/controller/ConsultaController.java
package med.voll.api.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import med.voll.api.consulta.*;
import med.voll.api.medico.MedicoRepository;
import med.voll.api.paciente.PacienteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("Consultas")
public class ConsultaController {

    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;
    private final ConsultaRepository consultaRepository;

    public ConsultaController(PacienteRepository pacienteRepository,
                              MedicoRepository medicoRepository,
                              ConsultaRepository consultaRepository) {
        this.pacienteRepository = pacienteRepository;
        this.medicoRepository = medicoRepository;
        this.consultaRepository = consultaRepository;
    }

    private record UsuarioCtx(String role, Long id) {}

    /** Lê email e role do SecurityContext (preenchido pelo JwtAuthFilter) e resolve o id. */
    private UsuarioCtx usuarioAtual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) return null;

        String email = auth.getName(); // principal: definimos como email no filtro
        boolean isMedico = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MEDICO"));
        boolean isPaciente = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_PACIENTE"));

        if (isMedico) {
            var m = medicoRepository.findByEmail(email).orElse(null);
            return (m == null) ? null : new UsuarioCtx("MEDICO", m.getId());
        }
        if (isPaciente) {
            var p = pacienteRepository.findByEmail(email).orElse(null);
            return (p == null) ? null : new UsuarioCtx("PACIENTE", p.getId());
        }
        // fallback (se o filtro não setar roles, tenta descobrir pelo email):
        var m = medicoRepository.findByEmail(email).orElse(null);
        if (m != null) return new UsuarioCtx("MEDICO", m.getId());
        var p = pacienteRepository.findByEmail(email).orElse(null);
        if (p != null) return new UsuarioCtx("PACIENTE", p.getId());
        return null;
    }

    @PostMapping("agendar")
    @PreAuthorize("hasAnyRole('MEDICO','PACIENTE')")
    @Transactional
    public ResponseEntity<?> agendar(@RequestBody @Valid DadosAgendarConsulta dados) {
        var ctx = usuarioAtual();
        if (ctx == null) return ResponseEntity.status(401).body("Não autenticado");

        Long medicoId, pacienteId;
        if ("MEDICO".equals(ctx.role())) {
            medicoId = ctx.id();
            if (dados.pacienteId() == null) return ResponseEntity.badRequest().body("pacienteId é obrigatório");
            pacienteId = dados.pacienteId();
        } else {
            pacienteId = ctx.id();
            if (dados.medicoId() == null) return ResponseEntity.badRequest().body("medicoId é obrigatório");
            medicoId = dados.medicoId();
        }

        var medico = medicoRepository.findById(medicoId).orElseThrow();
        var paciente = pacienteRepository.findById(pacienteId).orElseThrow();

        if (consultaRepository.existsByMedicoIdAndDataHoraAndStatus(medicoId, dados.dataHora(), Consulta.Status.AGENDADA))
            return ResponseEntity.badRequest().body("Médico já possui consulta nesse horário");
        if (consultaRepository.existsByPacienteIdAndDataHoraAndStatus(pacienteId, dados.dataHora(), Consulta.Status.AGENDADA))
            return ResponseEntity.badRequest().body("Paciente já possui consulta nesse horário");

        consultaRepository.save(new Consulta(medico, paciente, dados.dataHora(), dados.observacoes()));
        return ResponseEntity.ok().build();
    }

    @GetMapping("minhas-consultas")
    @PreAuthorize("hasAnyRole('MEDICO','PACIENTE')")
    public ResponseEntity<Page<DadosConsultaLista>> listar(
            @PageableDefault(page = 0, size = 10, sort = {"dataHora"}) Pageable p) {

        var ctx = usuarioAtual();
        if (ctx == null) return ResponseEntity.status(401).build();

        Page<Consulta> page = "MEDICO".equals(ctx.role())
                ? consultaRepository.findAllByMedicoIdOrderByDataHoraDesc(ctx.id(), p)
                : consultaRepository.findAllByPacienteIdOrderByDataHoraDesc(ctx.id(), p);

        var dto = page.map(c -> new DadosConsultaLista(
                c.getId(),
                c.getMedico().getNome(),
                c.getPaciente().getNome(),
                c.getDataHora(),
                c.getStatus().name()
        ));
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("deletar/{id}")
    @PreAuthorize("hasAnyRole('MEDICO','PACIENTE')")
    @Transactional
    public ResponseEntity<?> remover(@PathVariable Long id) {
        var ctx = usuarioAtual();
        if (ctx == null) return ResponseEntity.status(401).build();

        var consulta = consultaRepository.findById(id).orElse(null);
        if (consulta == null) return ResponseEntity.notFound().build();

        var pertenceAoMedico = "MEDICO".equals(ctx.role()) && consulta.getMedico().getId().equals(ctx.id());
        var pertenceAoPaciente = "PACIENTE".equals(ctx.role()) && consulta.getPaciente().getId().equals(ctx.id());
        if (!pertenceAoMedico && !pertenceAoPaciente) return ResponseEntity.status(403).build();

        consulta.cancelar();
        return ResponseEntity.noContent().build();
    }
}
