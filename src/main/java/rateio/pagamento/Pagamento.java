package rateio.pagamento;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Pagamento {
    private UUID id;
    private UUID grupoId;
    private UUID pagadorId;
    private UUID recebedorId;
    private BigDecimal valor;
    private Instant registradoEm;
    private boolean estornado;
    private Instant estornadoEm;

    public Pagamento(UUID grupoId, UUID pagadorId, UUID recebedorId, BigDecimal valor) {
        throw new UnsupportedOperationException("H5 - nao implementado");
    }

    /** H5: estorno logico. O pagamento permanece no extrato. */
    public void estornar() {
        throw new UnsupportedOperationException("H5 - nao implementado");
    }

    /** H3: pagamentos estornados nao entram no calculo de saldo. */
    public boolean estaAtivo() {
        throw new UnsupportedOperationException("H5 - nao implementado");
    }

    public boolean envolve(UUID membroId) {
        throw new UnsupportedOperationException("H5 - nao implementado");
    }

    /** H3: efeito do pagamento sobre o saldo do membro informado. */
    public BigDecimal efeitoSobre(UUID membroId) {
        throw new UnsupportedOperationException("H5 - nao implementado");
    }

    public UUID id() {
        throw new UnsupportedOperationException("H5 - nao implementado");
    }

    public UUID grupoId() {
        throw new UnsupportedOperationException("H5 - nao implementado");
    }

    public UUID pagadorId() {
        throw new UnsupportedOperationException("H5 - nao implementado");
    }

    public UUID recebedorId() {
        throw new UnsupportedOperationException("H5 - nao implementado");
    }

    public BigDecimal valor() {
        throw new UnsupportedOperationException("H5 - nao implementado");
    }

    public Instant registradoEm() {
        throw new UnsupportedOperationException("H5 - nao implementado");
    }
}
