package rateio.pagamento;

import jakarta.persistence.*;
import rateio.grupo.entity.GrupoEntidade;
import rateio.membro.Membro;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pagamento")
public class PagamentoEntidade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id", nullable = false)
    private GrupoEntidade grupo;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "de_id", nullable = false)
    private Membro de;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "para_id", nullable = false)
    private Membro para;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPagamento status;

    @Column(nullable = false)
    private Instant criadoEm;

    private Instant estornadoEm;

    public PagamentoEntidade() {}

    public UUID getId() {return id;}
    public void setId(UUID id) {this.id = id;}

    public Instant getEstornadoEm() {return estornadoEm;}
    public void setEstornadoEm(Instant estornadoEm) {this.estornadoEm = estornadoEm;}

    public Instant getCriadoEm() {return criadoEm;}
    public void setCriadoEm(Instant criadoEm) {this.criadoEm = criadoEm;}

    public StatusPagamento getStatus() {return status;}
    public void setStatus(StatusPagamento status) {this.status = status;}

    public BigDecimal getValor() {return valor;}
    public void setValor(BigDecimal valor) {this.valor = valor;}

    public Membro getPara() {return para;}
    public void setPara(Membro para) {this.para = para;}

    public Membro getDe() {return de;}
    public void setDe(Membro de) {this.de = de;}

    public GrupoEntidade getGrupo() {return grupo;}
    public void setGrupo(GrupoEntidade grupo) {this.grupo = grupo;}
}
