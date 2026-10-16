package rateio.despesa;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
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
}