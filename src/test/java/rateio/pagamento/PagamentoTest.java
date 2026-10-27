package rateio.pagamento;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class PagamentoTest {

    private static final BigDecimal TRINTA = new BigDecimal("30.00");

    private void rejeita(UUID grupo, UUID pagador, UUID recebedor, BigDecimal valor) {
        assertThrows(IllegalArgumentException.class,
                () -> new Pagamento(grupo, pagador, recebedor, valor));
    }

    @Test
    void rejeitaGrupoNulo() {
        rejeita(null, UUID.randomUUID(), UUID.randomUUID(), TRINTA);
    }

    @Test
    void rejeitaPagadorNulo() {
        rejeita(UUID.randomUUID(), null, UUID.randomUUID(), TRINTA);
    }

    @Test
    void rejeitaRecebedorNulo() {
        rejeita(UUID.randomUUID(), UUID.randomUUID(), null, TRINTA);
    }

    @Test
    void rejeitaPagadorIgualAoRecebedor() {
        UUID mesmo = UUID.randomUUID();
        rejeita(UUID.randomUUID(), mesmo, mesmo, TRINTA);
    }

    @Test
    void rejeitaValorNulo() {
        rejeita(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null);
    }

    @Test
    void rejeitaValorZero() {
        rejeita(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ZERO);
    }

    @Test
    void rejeitaValorNegativo() {
        rejeita(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("-10.00"));
    }
    @Test
    void criaPagamentoValido() {
        UUID grupo = UUID.randomUUID();
        UUID pagador = UUID.randomUUID();
        UUID recebedor = UUID.randomUUID();

        Pagamento pagamento = new Pagamento(grupo, pagador, recebedor, TRINTA);

        assertNotNull(pagamento.id());
        assertEquals(grupo, pagamento.grupoId());
        assertEquals(pagador, pagamento.pagadorId());
        assertEquals(recebedor, pagamento.recebedorId());
        assertEquals(0, pagamento.valor().compareTo(TRINTA));
        assertNotNull(pagamento.registradoEm());
        assertTrue(pagamento.estaAtivo());
    }
}
