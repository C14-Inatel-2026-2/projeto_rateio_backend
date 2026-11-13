package rateio.membro.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import rateio.membro.entity.Membro;
import rateio.membro.exception.CredenciaisInvalidasException;
import rateio.membro.exception.EmailJaCadastradoException;
import rateio.membro.repository.MembroRepository;

import java.util.Optional;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembroServiceTest {

    @Mock
    private MembroRepository repositorio;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private MembroService servico;

    @Test
    void cadastraMembroNomeVazio(){
        assertThrows(IllegalArgumentException.class,
                () -> servico.cadastrar("", "  Ana@X.com ", "123"));
    }

    @Test
    void cadastraMembroEmailVazio(){
        assertThrows(IllegalArgumentException.class,
                () -> servico.cadastrar("Ana", "", "123"));
    }

    @Test
    void cadastraMembroSenhaVazia(){
        assertThrows(IllegalArgumentException.class,
                () -> servico.cadastrar("Ana", " Ana@X.com ", ""));
    }

    @Test
    void rejeitaEmailRepetidoENaoSalva() {
        when(repositorio.existsByEmail("ana@x.com")).thenReturn(true);

        assertThrows(EmailJaCadastradoException.class,
                () -> servico.cadastrar("  Ana ", "  Ana@X.com ", "123"));

        verify(repositorio, never()).save(any());
    }

    @Test
    void cadastraMembroValido() {
        when(repositorio.existsByEmail("ana@x.com")).thenReturn(false);
        when(encoder.encode("123")).thenReturn("hash");
        when(repositorio.save(any(Membro.class))).thenAnswer(i -> i.getArgument(0));

        Membro salvo = servico.cadastrar("  Ana ", "  Ana@X.com ", "123");

        assertEquals("Ana", salvo.getNome());
        assertEquals("ana@x.com", salvo.getEmail());
        assertEquals("hash", salvo.getHashSenha());
    }

    @Test
    void autenticarComEmailNulo(){
        assertThrows(CredenciaisInvalidasException.class,
                () -> servico.autenticar(null,   "123"));
    }

    @Test
    void autenticarComSenhaNula(){
        assertThrows(CredenciaisInvalidasException.class,
                () -> servico.autenticar(" Ana@X.com ",   null));
    }

    @Test
    void autenticarComEmailInexistente(){
        when(repositorio.findByEmail("ana@x.com")).thenReturn(Optional.empty());

        assertThrows(CredenciaisInvalidasException.class,
                () -> servico.autenticar(" Ana@X.com ",   "123"));
    }

    @Test
    void autenticarComSenhaInvalida(){
        Membro membro = criarMembroPadrao();
        when(repositorio.findByEmail("ana@x.com")).thenReturn(Optional.of(membro));
        when(encoder.matches("errada", "hash")).thenReturn(false);

        assertThrows(CredenciaisInvalidasException.class,
                () -> servico.autenticar("Ana@X.com", "errada"));
    }

    @Test
    void autenticarComCredenciaisCorretas(){
        Membro membro = criarMembroPadrao();
        when(repositorio.findByEmail("ana@x.com")).thenReturn(Optional.of(membro));
        when(encoder.matches("123", "hash")).thenReturn(true);

        Membro autenticado = servico.autenticar("  Ana@X.com ", "123");

        assertEquals(membro, autenticado);
    }

    private Membro criarMembroPadrao() {
        Membro membro = new Membro();
        membro.setEmail("ana@x.com");
        membro.setHashSenha("hash");
        return membro;
    }
}