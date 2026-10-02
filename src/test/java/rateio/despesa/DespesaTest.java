package rateio.despesa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DespesaTest {

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

        Despesa despesa = new Despesa(
                grupoId,
                pagadorId,
                "Mercado",
                new BigDecimal("200.00"),
                Despesa.EstrategiaDivisao.IGUAL,
                List.of()
        );

        despesa.estornar();

        org.junit.jupiter.api.Assertions.assertFalse(despesa.estaAtiva());
    }

}