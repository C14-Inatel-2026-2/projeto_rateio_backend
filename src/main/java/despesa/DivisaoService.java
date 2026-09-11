package despesa;

import java.util.ArrayList;
import java.util.List;

public class DivisaoService {

    public List<Double> dividir(double valorTotal, int numeroPessoas) {
        List<Double> parcelas = new ArrayList<>();

        // Converte o valor em reais para centavos inteiros (ex: 100.00 vira 10000)
        int totalCentavos = (int) Math.round(valorTotal * 100);

        // Descobre quanto cada pessoa paga como base e a sobra que restou
        int centavosPorPessoa = totalCentavos / numeroPessoas;
        int centavosSobrando = totalCentavos % numeroPessoas;

        for (int i = 0; i < numeroPessoas; i++) {
            int valorPessoaCentavos = centavosPorPessoa;

            // Distribui 1 centavo extra para os primeiros da lista até zerar a sobra
            if (i < centavosSobrando) {
                valorPessoaCentavos += 1;
            }

            // Converte os centavos de volta para o formato em reais
            parcelas.add(valorPessoaCentavos / 100.0);
        }

        return parcelas;
    }
}