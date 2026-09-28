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

        List<Double> resultado = service.dividir(valorTotal, pessoas);

        assertEquals(3, resultado.size());

        double somaDasParcelas = 0.0;
        for (double parcela : resultado) {
            somaDasParcelas += parcela;
        }

        assertEquals(valorTotal, somaDasParcelas, 0.001);
    }
}