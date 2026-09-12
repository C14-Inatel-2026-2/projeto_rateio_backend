package despesa;

import java.util.ArrayList;
import java.util.List;

public class DivisaoService {

    public List<Double> dividir(double valorTotal, int numeroPessoas) {
        List<Double> parcelas = new ArrayList<>();

        int totalCentavos = (int) Math.round(valorTotal * 100);

        int centavosPorPessoa = totalCentavos / numeroPessoas;
        int centavosSobrando = totalCentavos % numeroPessoas;

        for (int i = 0; i < numeroPessoas; i++) {
            int valorPessoaCentavos = centavosPorPessoa;

            if (i < centavosSobrando) {
                valorPessoaCentavos += 1;
            }

            parcelas.add(valorPessoaCentavos / 100.0);
        }

        return parcelas;
    }
}