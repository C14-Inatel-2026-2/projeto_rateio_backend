package rateio.membro;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MembroService {

    private final MembroRepository repositorio;
    private final PasswordEncoder encoder;

    public MembroService(MembroRepository repositorio, PasswordEncoder encoder) {
        this.repositorio = repositorio;
        this.encoder = encoder;
    }

    @Transactional
    public Membro cadastrar(String nome, String email, String senha) {
        if (nome == null || nome.isBlank()
                || email == null || email.isBlank()
                || senha == null || senha.isBlank()) {
            throw new IllegalArgumentException("Nome, email e senha sao obrigatorios");
        }

        String emailNormalizado = email.trim().toLowerCase();

        if (repositorio.existsByEmail(emailNormalizado)) {
            throw new EmailJaCadastradoException();
        }

        Membro membro = new Membro();
        membro.setNome(nome.trim());
        membro.setEmail(emailNormalizado);
        membro.setHashSenha(encoder.encode(senha));

        return repositorio.save(membro);
    }

    public Membro autenticar(String email, String senha) {
        if (email == null || senha == null) {
            throw new CredenciaisInvalidasException();
        }

        String emailNormalizado = email.trim().toLowerCase();

        Membro membro = repositorio.findByEmail(emailNormalizado)
                .orElseThrow(CredenciaisInvalidasException::new);

        if (!encoder.matches(senha, membro.getHashSenha())) {
            throw new CredenciaisInvalidasException();
        }

        return membro;
    }
}
