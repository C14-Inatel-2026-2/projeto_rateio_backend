package rateio.despesa;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DivisaoService {

    public List<Despesa.Parcela> dividirIgual(BigDecimal valorTotal, List<UUID> membrosIds) {
        if (membrosIds == null || membrosIds.isEmpty()) {
            throw new IllegalArgumentException("A lista de membros nao pode ser vazia.");
        }

        int numeroPessoas = membrosIds.size();

        long totalCentavos = valorTotal.setScale(2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        long centavosPorPessoa = totalCentavos / numeroPessoas;
        long centavosSobrando = totalCentavos % numeroPessoas;

        List<Despesa.Parcela> parcelas = new ArrayList<>();

        for (int i = 0; i < numeroPessoas; i++) {
            long valorPessoaCentavos = centavosPorPessoa;

            if (i < centavosSobrando) {
                valorPessoaCentavos += 1;
            }

            BigDecimal valorParcela = BigDecimal.valueOf(valorPessoaCentavos, 2);
            parcelas.add(new Despesa.Parcela(membrosIds.get(i), valorParcela));
        }

        return parcelas;
    }

    public List<Despesa.Parcela> dividirPorPeso(BigDecimal valorTotal, Map<UUID, BigDecimal> pesos) {
        if (valorTotal == null || valorTotal.signum() <= 0) {
            throw new IllegalArgumentException("O valor total deve ser positivo.");
        }
        if (pesos == null || pesos.isEmpty()) {
            throw new IllegalArgumentException("A lista de membros nao pode ser vazia.");
        }

        BigDecimal somaPesos = BigDecimal.ZERO;
        for (BigDecimal peso : pesos.values()) {
            if (peso == null || peso.signum() <= 0) {
                throw new IllegalArgumentException("O peso deve ser positivo.");
            }
            somaPesos = somaPesos.add(peso);
        }

        long totalCentavos = valorTotal.setScale(2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();
        BigDecimal totalDecimal = BigDecimal.valueOf(totalCentavos);

        List<UUID> ids = new ArrayList<>(pesos.keySet());
        long[] centavos = new long[ids.size()];
        BigDecimal[] restos = new BigDecimal[ids.size()];
        long distribuido = 0;

        for (int i = 0; i < ids.size(); i++) {
            BigDecimal[] divisao = totalDecimal
                    .multiply(pesos.get(ids.get(i)))
                    .divideAndRemainder(somaPesos);
            centavos[i] = divisao[0].longValueExact();
            restos[i] = divisao[1];
            distribuido += centavos[i];
        }

        List<Integer> ordem = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            ordem.add(i);
        }
        ordem.sort((a, b) -> restos[b].compareTo(restos[a]));

        long sobra = totalCentavos - distribuido;
        for (int k = 0; k < sobra; k++) {
            centavos[ordem.get(k)]++;
        }

        List<Despesa.Parcela> parcelas = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            parcelas.add(new Despesa.Parcela(ids.get(i), BigDecimal.valueOf(centavos[i], 2)));
        }
        return parcelas;
    }
}