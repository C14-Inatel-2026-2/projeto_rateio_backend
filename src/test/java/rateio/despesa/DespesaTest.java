package rateio.despesa;

import static rateio.despesa.Despesa.EstrategiaDivisao.IGUAL;
import static rateio.despesa.Despesa.EstrategiaDivisao.VALORES_FIXOS;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DespesaTest {

    private static final BigDecimal CINQUENTA = new BigDecimal("50.00");

    private Despesa.Parcela parcela(UUID membroId, String valor) {
        return new Despesa.Parcela(membroId, new BigDecimal(valor));
    }

    private Despesa despesaDeDois(UUID pagador, UUID outro) {
        return new Despesa(UUID.randomUUID(), pagador, "Mercado", new BigDecimal("100.00"),
                IGUAL, List.of(parcela(pagador, "50.00"), parcela(outro, "50.00")));
    }

    private void rejeita(UUID grupo, UUID pagador, String descricao, BigDecimal total,
                         Despesa.EstrategiaDivisao estrategia, List<Despesa.Parcela> parcelas) {
        assertThrows(IllegalArgumentException.class,
                () -> new Despesa(grupo, pagador, descricao, total, estrategia, parcelas));
    }

    @Test
    void rejeitaGrupoNulo() {
        UUID p = UUID.randomUUID();
        rejeita(null, p, "Mercado", CINQUENTA, IGUAL, List.of(parcela(p, "50.00")));
    }


    @Test
    void rejeitaPagadorNulo() {
        rejeita(UUID.randomUUID(), null, "Mercado", CINQUENTA, IGUAL,
                List.of(parcela(UUID.randomUUID(), "50.00")));
    }

    @Test
    void rejeitaDescricaoVazia() {
        UUID p = UUID.randomUUID();
        rejeita(UUID.randomUUID(), p, " ", CINQUENTA, IGUAL, List.of(parcela(p, "50.00")));
    }

    @Test
    void rejeitaValorZero() {
        UUID p = UUID.randomUUID();
        rejeita(UUID.randomUUID(), p, "Mercado", BigDecimal.ZERO, IGUAL, List.of(parcela(p, "0.00")));
    }

    @Test
    void rejeitaValorNegativo() {
        UUID p = UUID.randomUUID();
        rejeita(UUID.randomUUID(), p, "Mercado", new BigDecimal("-50.00"), IGUAL,
                List.of(parcela(p, "-50.00")));
    }

    @Test
    void rejeitaEstrategiaNula() {
        UUID p = UUID.randomUUID();
        rejeita(UUID.randomUUID(), p, "Mercado", CINQUENTA, null, List.of(parcela(p, "50.00")));
    }

    @Test
    void rejeitaSemParcelas() {
        rejeita(UUID.randomUUID(), UUID.randomUUID(), "Mercado", CINQUENTA, IGUAL, List.of());
    }

    @Test
    void rejeitaParcelaNegativa() {
        UUID p = UUID.randomUUID();
        rejeita(UUID.randomUUID(), p, "Mercado", CINQUENTA, IGUAL,
                List.of(parcela(p, "100.00"), parcela(UUID.randomUUID(), "-50.00")));
    }

    @Test
    void rejeitaMembroRepetido() {
        UUID p = UUID.randomUUID();
        rejeita(UUID.randomUUID(), p, "Mercado", CINQUENTA, IGUAL,
                List.of(parcela(p, "25.00"), parcela(p, "25.00")));
    }
    @Test
    void rejeitaSomaDiferente() {
        UUID p = UUID.randomUUID();
        rejeita(UUID.randomUUID(), p, "Mercado", CINQUENTA, VALORES_FIXOS,
                List.of(parcela(p, "30.00"), parcela(UUID.randomUUID(), "10.00")));
    }


    @Test
    @DisplayName("Deve retornar true em parcelasFecham quando a soma das parcelas for igual ao valor total")
    void deveRetornarTrueQuandoSomaDasParcelasForIgualAoValorTotal() {
        UUID grupoId = UUID.randomUUID();
        UUID pagadorId = UUID.randomUUID();
        UUID membro1 = UUID.randomUUID();
        UUID membro2 = UUID.randomUUID();

        BigDecimal valorTotal = new BigDecimal("100.00");
        List<Despesa.Parcela> parcelas = List.of(
                new Despesa.Parcela(membro1, new BigDecimal("60.00")),
                new Despesa.Parcela(membro2, new BigDecimal("40.00"))
        );

        Despesa despesa = new Despesa(
                grupoId,
                pagadorId,
                "Jantar de equipe",
                valorTotal,
                Despesa.EstrategiaDivisao.VALORES_FIXOS,
                parcelas
        );

        assertTrue(despesa.parcelasFecham());

    }

    @Test
    @DisplayName("Deve retornar o valor correto devido por um membro em parcelaDe e zero para nao participantes")
    void deveRetornarValorDevidoPorMembroEZeroParaNaoParticipante() {
        UUID grupoId = UUID.randomUUID();
        UUID pagadorId = UUID.randomUUID();
        UUID membroParticipante = UUID.randomUUID();
        UUID membroNaoParticipante = UUID.randomUUID();
        BigDecimal valorParcela = new BigDecimal("45.50");

        List<Despesa.Parcela> parcelas = List.of(
                new Despesa.Parcela(membroParticipante, valorParcela)
        );

        Despesa despesa = new Despesa(
                grupoId,
                pagadorId,
                "Uber compartilhado",
                valorParcela,
                Despesa.EstrategiaDivisao.IGUAL,
                parcelas
        );

        org.junit.jupiter.api.Assertions.assertEquals(valorParcela, despesa.parcelaDe(membroParticipante));
        org.junit.jupiter.api.Assertions.assertEquals(BigDecimal.ZERO, despesa.parcelaDe(membroNaoParticipante));
    }

    @Test
    @DisplayName("Deve inativar a despesa ao executar o estorno")
    void deveInativarDespesaAoEstornar() {
        UUID grupoId = UUID.randomUUID();
        UUID pagadorId = UUID.randomUUID();
        UUID outroMembroId = UUID.randomUUID();

        Despesa despesa = new Despesa(
                grupoId,
                pagadorId,
                "Mercado",
                new BigDecimal("200.00"),
                Despesa.EstrategiaDivisao.IGUAL,
                List.of(
                        new Despesa.Parcela(pagadorId, new BigDecimal("100.00")),
                        new Despesa.Parcela(outroMembroId, new BigDecimal("100.00"))
                )
        );

        despesa.estornar();

        org.junit.jupiter.api.Assertions.assertFalse(despesa.estaAtiva());
    }

    @Test
    @DisplayName("Deve garantir a imutabilidade da lista de parcelas retornada")
    void deveLancarExcecaoAoTentarModificarListaDeParcelasRetornada() {
        UUID grupoId = UUID.randomUUID();
        UUID pagadorId = UUID.randomUUID();

        Despesa despesa = new Despesa(
                grupoId,
                pagadorId,
                "Cinema",
                new BigDecimal("50.00"),
                Despesa.EstrategiaDivisao.IGUAL,
                List.of(new Despesa.Parcela(UUID.randomUUID(), new BigDecimal("50.00")))
        );

        List<Despesa.Parcela> parcelas = despesa.parcelas();

        org.junit.jupiter.api.Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> parcelas.add(new Despesa.Parcela(UUID.randomUUID(), new BigDecimal("10.00")))
        );
    }
}