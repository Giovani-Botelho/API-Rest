package med.voll.api.controller;

import jakarta.transaction.TransactionScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import med.voll.api.auth.DadosLogin;
import med.voll.api.auth.DadosToken;
import med.voll.api.paciente.*;
import med.voll.api.security.TokenService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("pacientes")
public class PacienteController {

    private final PacienteRepository repository;
    private final PasswordEncoder encoder;
    private final TokenService tokens;

    public PacienteController(PacienteRepository repository, PasswordEncoder encoder, TokenService tokens) {
        this.repository = repository;
        this.encoder = encoder;
        this.tokens = tokens;
    }

    @PostMapping("cadastro")
    @Transactional
    public void cadastrar(@RequestBody @Valid DadosCadastroPaciente dados) {
        var paciente = new Paciente(dados);
        paciente.setSenha(encoder.encode(dados.senha()));
        repository.save(paciente);
    }

    @PostMapping("login")
    public ResponseEntity<DadosToken> login(@RequestBody @Valid DadosLogin cred) {
        var paciente = repository.findByEmail(cred.email())
                .orElseThrow(() -> new RuntimeException("Paciente não encontrado"));
        if (!encoder.matches(cred.senha(), paciente.getSenha())) {
            return ResponseEntity.status(401).build();
        }
        var jwt = tokens.gerarToken("PACIENTE", paciente.getEmail());
        return ResponseEntity.ok(new DadosToken(jwt));
    }

    @GetMapping("listagem")
    @PreAuthorize("hasRole('PACIENTE')")
    public Page<DadosListagemPaciente> listar(@PageableDefault(page = 0, size = 10, sort = {"nome"}) Pageable paginacao) {
        return repository.findAll(paginacao).map(DadosListagemPaciente::new);
    }

    @PutMapping("atualizacao")
    @Transactional
    @PreAuthorize("hasRole('PACIENTE')")
    public void atualizar(@RequestBody @Valid DadosAtualizacaoPaciente dados) {
        var paciente = repository.getReferenceById(dados.id());
        paciente.atualizarInformacoes(dados);
    }

    @DeleteMapping("deletar/{id}")
    @Transactional
    @PreAuthorize("hasRole('PACIENTE')")
    public void remover(@PathVariable Long id) {
        var paciente = repository.getReferenceById(id);
        paciente.inativar();
    }
}

