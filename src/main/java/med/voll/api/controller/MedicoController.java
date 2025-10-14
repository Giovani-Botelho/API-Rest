package med.voll.api.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import med.voll.api.auth.DadosLogin;
import med.voll.api.auth.DadosToken;
import med.voll.api.medico.*;
import med.voll.api.security.TokenService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/medicos")
public class MedicoController {

    private final MedicoRepository repository;
    private final PasswordEncoder encoder;
    private final TokenService tokens;

    public MedicoController(MedicoRepository repository, PasswordEncoder encoder, TokenService tokens) {
        this.repository = repository;
        this.encoder = encoder;
        this.tokens = tokens;
    }

    @PostMapping("cadastro")
    @Transactional
    public void cadastrar(@RequestBody @Valid DadosCadastroMedico dados) {
        var medico = new Medico(dados);
        medico.setSenha(encoder.encode(dados.senha())); // codifica a senha antes de salvar
        repository.save(medico);
    }

    @PostMapping("login")
    public ResponseEntity<DadosToken> login(@RequestBody @Valid DadosLogin cred) {
        var medico = repository.findByEmail(cred.email())
                .orElseThrow(() -> new RuntimeException("Médico não encontrado"));

        if (!encoder.matches(cred.senha(), medico.getSenha())) {
            return ResponseEntity.status(401).build();
        }

        var jwt = tokens.gerarToken("MEDICO", medico.getEmail());
        return ResponseEntity.ok(new DadosToken(jwt));
    }

    @GetMapping("listagem")
    @PreAuthorize("hasAnyRole('MEDICO','PACIENTE')")
    public Page<DadosListagemMedico> listar(@PageableDefault(size = 10, sort = {"nome"}) Pageable paginacao) {
        return repository.findAllByAtivoTrue(paginacao).map(DadosListagemMedico::new);
    }

    @PutMapping("atualizacao")
    @Transactional
    @PreAuthorize("hasRole('MEDICO')")
    public void atualizar(@RequestBody @Valid DadosAtualizacaoMedico dados) {
        var medico = repository.getReferenceById(dados.id());
        medico.atualizarInformacoes(dados);
    }

    @DeleteMapping("deletar/{id}")
    @Transactional
    @PreAuthorize("hasRole('MEDICO')")
    public void excluir(@PathVariable Long id) {
        var medico = repository.getReferenceById(id);
        medico.excluir();
    }
}
