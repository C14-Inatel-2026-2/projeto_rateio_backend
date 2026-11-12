package rateio.despesa.service;

import org.springframework.stereotype.Service;
import rateio.despesa.domain.Despesa.Parcela;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

// As divisoes sao feitas em centavos inteiros para que a soma das parcelas feche exatamente com o total.
@Service
public class DivisaoService {

    private static final int CASAS_DECIMAIS = 2;

    public List<Parcela> dividirIgual(BigDecimal valorTotal, List<UUID> membrosIds) {
        if (membrosIds == null || membrosIds.isEmpty()) {
            throw new IllegalArgumentException("A lista de membros nao pode ser vazia.");
        }

        long totalCentavos = emCentavos(valorTotal);
        long centavosPorPessoa = totalCentavos / membrosIds.size();
        long centavosSobrando = totalCentavos % membrosIds.size();

        List<Parcela> parcelas = new ArrayList<>();
        for (int i = 0; i < membrosIds.size(); i++) {
            long centavos = i < centavosSobrando ? centavosPorPessoa + 1 : centavosPorPessoa;
            parcelas.add(new Parcela(membrosIds.get(i), emReais(centavos)));
        }
        return parcelas;
    }

    public List<Parcela> dividirPorPeso(BigDecimal valorTotal, Map<UUID, BigDecimal> pesos) {
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

        long totalCentavos = emCentavos(valorTotal);
        List<UUID> ids = new ArrayList<>(pesos.keySet());
        long[] centavos = new long[ids.size()];
        BigDecimal[] restos = new BigDecimal[ids.size()];
        long distribuido = 0;

        for (int i = 0; i < ids.size(); i++) {
            BigDecimal[] quocienteEResto = BigDecimal.valueOf(totalCentavos)
                    .multiply(pesos.get(ids.get(i)))
                    .divideAndRemainder(somaPesos);
            centavos[i] = quocienteEResto[0].longValueExact();
            restos[i] = quocienteEResto[1];
            distribuido += centavos[i];
        }

        // Metodo do maior resto: os centavos que sobram do truncamento vao para quem mais perdeu
        // com ele. A ordenacao e estavel, entao empates favorecem quem aparece primeiro.
        List<Integer> porMaiorResto = IntStream.range(0, ids.size())
                .boxed()
                .sorted(Comparator.comparing((Integer i) -> restos[i]).reversed())
                .toList();
        long sobra = totalCentavos - distribuido;
        for (int k = 0; k < sobra; k++) {
            centavos[porMaiorResto.get(k)]++;
        }

        List<Parcela> parcelas = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            parcelas.add(new Parcela(ids.get(i), emReais(centavos[i])));
        }
        return parcelas;
    }

    public List<Parcela> dividirPorValoresFixos(BigDecimal valorTotal, Map<UUID, BigDecimal> valores) {
        List<Parcela> parcelas = new ArrayList<>();
        BigDecimal soma = BigDecimal.ZERO;
        for (Map.Entry<UUID, BigDecimal> entrada : valores.entrySet()) {
            BigDecimal valor = entrada.getValue();
            if (valor == null || valor.signum() <= 0 || valor.scale() > CASAS_DECIMAIS) {
                throw new IllegalArgumentException("Valor invalido");
            }
            soma = soma.add(valor);
            parcelas.add(new Parcela(entrada.getKey(), valor));
        }
        if (soma.compareTo(valorTotal) != 0) {
            throw new IllegalArgumentException("Soma das parcelas diferente do total");
        }
        return parcelas;
    }

    private static long emCentavos(BigDecimal valor) {
        return valor.setScale(CASAS_DECIMAIS, RoundingMode.HALF_UP)
                .movePointRight(CASAS_DECIMAIS)
                .longValueExact();
    }

    private static BigDecimal emReais(long centavos) {
        return BigDecimal.valueOf(centavos, CASAS_DECIMAIS);
    }
}
