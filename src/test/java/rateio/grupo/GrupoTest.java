package rateio.grupo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GrupoTest {
    @Mock
    private Clock relogio;

    @Test
    public void adicionarMembroTest() {
        UUID ana        = UUID.randomUUID();
        UUID bruno      = UUID.randomUUID();
        Grupo grupo     = new Grupo("Teste", Set.of(ana));

        grupo.adicionarMembro(bruno);

        assertEquals(2, grupo.getQuantidadeDeMembros());
        assertTrue(grupo.contem(bruno));
    }

    @Test
    public void contemNegativoTest() {
        UUID joao       = UUID.randomUUID();
        UUID livia      = UUID.randomUUID();
        UUID leticia    = UUID.randomUUID();
        Grupo grupo     = new Grupo("Viagem", Set.of(joao));

        grupo.adicionarMembro(livia);

        assertEquals(2, grupo.getQuantidadeDeMembros());
        assertFalse(grupo.contem(leticia));
    }

    @Test
    public void adicionarMembroDuplicadoNegativoTest() {
        UUID ana        = UUID.randomUUID();
        Grupo grupo     = new Grupo("Viagem", Set.of(ana));

        assertThrows(IllegalStateException.class, () -> grupo.adicionarMembro(ana));
        assertEquals(1, grupo.getQuantidadeDeMembros());
    }

    @Test
    public void getDataCriacaoMockTest() {
        Instant instanteFixo    = Instant.parse("2026-01-15T10:00:00Z");
        when(relogio.instant()).thenReturn(instanteFixo);

        Grupo grupo             = new Grupo("Viagem", Set.of(UUID.randomUUID()), relogio);

        assertEquals(instanteFixo, grupo.getDataCriacao());
        verify(relogio, times(1)).instant();
    }

    @Test
    public void getDataCriacaoAposAlterarMembrosMockTest() {
        Instant criacao = Instant.parse("2026-01-15T10:00:00Z");
        Instant depois  = Instant.parse("2026-02-20T18:30:00Z");
        when(relogio.instant()).thenReturn(criacao, depois);
        UUID ana        = UUID.randomUUID();
        Grupo grupo     = new Grupo("Viagem", Set.of(ana), relogio);

        grupo.adicionarMembro(UUID.randomUUID());
        grupo.removerMembro(ana);

        assertEquals(criacao, grupo.getDataCriacao());
        verify(relogio, times(1)).instant();
    }
}
