package rateio.pagamento;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class PagamentoTest {

    private static final BigDecimal TRINTA = new BigDecimal("30.00");

    private void rejeita(UUID grupo, UUID pagador, UUID recebedor, BigDecimal valor) {
        assertThrows(IllegalArgumentException.class,
                () -> new Pagamento(grupo, pagador, recebedor, valor));
    }

    private Pagamento novoPagamento(UUID pagador, UUID recebedor) {
        return new Pagamento(UUID.randomUUID(), pagador, recebedor, TRINTA);
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

    @Test
    void estornoRepetido(){
        Pagamento pagamento = novoPagamento(UUID.randomUUID(), UUID.randomUUID());
        pagamento.estornar();
        assertThrows(IllegalStateException.class, () -> pagamento.estornar());
    }

    @Test
    void estornoRepetidoMantemDataOriginal() {
        Pagamento pagamento = novoPagamento(UUID.randomUUID(), UUID.randomUUID());
        pagamento.estornar();
        Instant dataOriginal = pagamento.estornadoEm();
        assertThrows(IllegalStateException.class, () -> pagamento.estornar());
        assertEquals(dataOriginal, pagamento.estornadoEm());
    }

    @Test
    void estornoRealizado(){
        Pagamento pagamento = novoPagamento(UUID.randomUUID(), UUID.randomUUID());
        pagamento.estornar();
        assertTrue(pagamento.estornado());
        assertNotNull(pagamento.estornadoEm());
    }

    @Test
    void nasceAtivo() {
        Pagamento pagamento = novoPagamento(UUID.randomUUID(), UUID.randomUUID());
        assertTrue(pagamento.estaAtivo());
        assertFalse(pagamento.estornado());
        assertNull(pagamento.estornadoEm());
    }

    @Test
    void inativoAposEstorno() {
        Pagamento pagamento = novoPagamento(UUID.randomUUID(), UUID.randomUUID());
        pagamento.estornar();
        assertFalse(pagamento.estaAtivo());
    }

    @Test
    void envolvePagador() {
        UUID pagador = UUID.randomUUID();
        UUID recebedor = UUID.randomUUID();
        assertTrue(novoPagamento(pagador, recebedor).envolve(pagador));
    }

    @Test
    void envolveRecebedor() {
        UUID pagador = UUID.randomUUID();
        UUID recebedor = UUID.randomUUID();
        assertTrue(novoPagamento(pagador, recebedor).envolve(recebedor));
    }

    @Test
    void naoEnvolveTerceiro() {
        Pagamento pagamento = novoPagamento(UUID.randomUUID(), UUID.randomUUID());
        assertFalse(pagamento.envolve(UUID.randomUUID()));
    }

    @Test
    void envolveNulo() {
        Pagamento pagamento = novoPagamento(UUID.randomUUID(), UUID.randomUUID());
        assertFalse(pagamento.envolve(null));
    }

    @Test
    void efeitoSobrePagador() {
        UUID pagador = UUID.randomUUID();
        UUID recebedor = UUID.randomUUID();
        Pagamento pagamento = novoPagamento(pagador, recebedor);
        assertEquals(0, pagamento.efeitoSobre(pagador).compareTo(TRINTA));
    }

    @Test
    void efeitoSobreRecebedor() {
        UUID pagador = UUID.randomUUID();
        UUID recebedor = UUID.randomUUID();
        Pagamento pagamento = novoPagamento(pagador, recebedor);
        assertEquals(0, pagamento.efeitoSobre(recebedor).compareTo(TRINTA.negate()));
    }

    @Test
    void efeitoSobreTerceiro() {
        Pagamento pagamento = novoPagamento(UUID.randomUUID(), UUID.randomUUID());
        assertEquals(0, pagamento.efeitoSobre(UUID.randomUUID()).signum());
    }

    @Test
    void efeitoSobreNulo() {
        Pagamento pagamento = novoPagamento(UUID.randomUUID(), UUID.randomUUID());
        assertEquals(0, pagamento.efeitoSobre(null).signum());
    }

    @Test
    void efeitosSomamZero() {
        UUID pagador = UUID.randomUUID();
        UUID recebedor = UUID.randomUUID();
        Pagamento pagamento = novoPagamento(pagador, recebedor);
        BigDecimal soma = pagamento.efeitoSobre(pagador).add(pagamento.efeitoSobre(recebedor));
        assertEquals(0, soma.signum());
    }

}
