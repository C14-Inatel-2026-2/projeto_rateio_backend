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
        if (grupoId == null || pagadorId == null || recebedorId == null) {
            throw new IllegalArgumentException("Grupo, pagador e recebedor sao obrigatorios.");
        }
        if (pagadorId.equals(recebedorId)) {
            throw new IllegalArgumentException("Pagador e recebedor devem ser diferentes.");
        }
        if (valor == null || valor.signum() <= 0) {
            throw new IllegalArgumentException("O valor deve ser positivo.");
        }
        this.id = UUID.randomUUID();
        this.grupoId = grupoId;
        this.pagadorId = pagadorId;
        this.recebedorId = recebedorId;
        this.valor = valor;
        this.registradoEm = Instant.now();
        this.estornado = false;
    }


    /** H5: estorno logico. O pagamento permanece no extrato. */
    public void estornar() {
        if (estornado) {
            throw new IllegalStateException("O pagamento ja foi estornado.");
        }
        this.estornado = true;
        this.estornadoEm = Instant.now();
    }

    /** H3: pagamentos estornados nao entram no calculo de saldo. */
    public boolean estaAtivo() {
        return !estornado;
    }

    public boolean envolve(UUID membroId) {
        if (membroId == null) {
            return false;
        }
        if (membroId.equals(pagadorId)) {
            return true;
        }
        if (membroId.equals(recebedorId)) {
            return true;
        }
        return false;
    }

    /** H3: efeito do pagamento sobre o saldo do membro informado. */
    public BigDecimal efeitoSobre(UUID membroId) {
        if (membroId == null) {
            return BigDecimal.ZERO;
        }
        if (membroId.equals(pagadorId)) {
            return valor;
        }
        if (membroId.equals(recebedorId)) {
            return valor.negate();
        }
        return BigDecimal.ZERO;
    }

    public UUID id() {
        return id;
    }

    public UUID grupoId() {
        return grupoId;
    }

    public UUID pagadorId() {
        return pagadorId;
    }

    public UUID recebedorId() {
        return recebedorId;
    }

    public BigDecimal valor() {
        return valor;
    }

    public Instant registradoEm() {
        return registradoEm;
    }

    public boolean estornado(){ return estornado;}

    public Instant estornadoEm() { return estornadoEm;}
}
