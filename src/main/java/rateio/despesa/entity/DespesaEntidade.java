package rateio.despesa.entity;

import jakarta.persistence.*;
import rateio.despesa.domain.Despesa;
import rateio.despesa.domain.StatusDespesa;
import rateio.grupo.entity.GrupoEntidade;
import rateio.membro.Membro;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "despesa")
public class DespesaEntidade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id", nullable = false)
    private GrupoEntidade grupo;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pagador_id", nullable = false)
    private Membro pagador;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por_id", nullable = false)
    private Membro registrador;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Despesa.EstrategiaDivisao estrategia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusDespesa status;

    @Column(nullable = false)
    private Instant criadaEm;

    private Instant estornadaEm;

    public DespesaEntidade() {}

    public UUID getId() {return id;}
    public void setId(UUID id) {this.id = id;}

    public GrupoEntidade getGrupo() {return grupo;}
    public void setGrupo(GrupoEntidade grupo) {this.grupo = grupo;}

    public Membro getPagador() {return pagador;}
    public void setPagador(Membro pagador) {this.pagador = pagador;}

    public Membro getRegistrador() {return registrador;}
    public void setRegistrador(Membro registrador) {this.registrador = registrador;}

    public String getDescricao() {return descricao;}
    public void setDescricao(String descricao) {this.descricao = descricao;}

    public BigDecimal getValorTotal() {return valorTotal;}
    public void setValorTotal(BigDecimal valorTotal) {this.valorTotal = valorTotal;}

    public Despesa.EstrategiaDivisao getEstrategia() {return estrategia;}
    public void setEstrategia(Despesa.EstrategiaDivisao estrategia) {this.estrategia = estrategia;}

    public StatusDespesa getStatus() {return status;}
    public void setStatus(StatusDespesa status) {this.status = status;}

    public Instant getCriadaEm() {return criadaEm;}
    public void setCriadaEm(Instant criadaEm) {this.criadaEm = criadaEm;}

    public Instant getEstornadaEm() {return estornadaEm;}
    public void setEstornadaEm(Instant estornadaEm) {this.estornadaEm = estornadaEm;}
}
