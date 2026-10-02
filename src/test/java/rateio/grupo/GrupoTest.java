package rateio.grupo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class GrupoTest {
    @Test
    public void adicionarMembroTest() {
        UUID ana        = UUID.randomUUID();
        UUID bruno      = UUID.randomUUID();
        Grupo grupo     = new Grupo("Teste", Set.of(ana));

        grupo.adicionarMembro(bruno);

        assertEquals(2, grupo.getQuantidadeDeMembros());
        assertTrue(grupo.contem(bruno));
    }
}
