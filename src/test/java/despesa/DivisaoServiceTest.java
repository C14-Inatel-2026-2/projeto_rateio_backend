package despesa;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DivisaoServiceTest {

    @Test
    public void testarDivisaoCemPorTres() {
        DivisaoService service = new DivisaoService();

        double valorTotal = 100.0;
        int pessoas = 3;

        // Executa a função de divisão
        List<Double> resultado = service.dividir(valorTotal, pessoas);

        // Valida se a quantidade de parcelas bate com a quantidade de pessoas
        assertEquals(3, resultado.size());

        // Soma os valores calculados de cada pessoa
        double somaDasParcelas = 0.0;
        for (double parcela : resultado) {
            somaDasParcelas += parcela;
        }

        // Garante que a soma das parcelas seja exatamente igual ao valor original
        assertEquals(valorTotal, somaDasParcelas, 0.001);
    }
}