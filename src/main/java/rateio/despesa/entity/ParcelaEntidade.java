package rateio.despesa.entity;

import jakarta.persistence.*;
import rateio.membro.entity.Membro;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "parcela",
        uniqueConstraints = @UniqueConstraint(columnNames = {"despesa_id", "membro_id"}))
public class ParcelaEntidade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "despesa_id", nullable = false)
    private DespesaEntidade despesa;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "membro_id", nullable = false)
    private Membro membro;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    public ParcelaEntidade() {}

    public UUID getId() {return id;}
    public void setId(UUID id) {this.id = id;}

    public DespesaEntidade getDespesa() {return despesa;}
    public void setDespesa(DespesaEntidade despesa) {this.despesa = despesa;}

    public Membro getMembro() {return membro;}
    public void setMembro(Membro membro) {this.membro = membro;}

    public BigDecimal getValor() {return valor;}
    public void setValor(BigDecimal valor) {this.valor = valor;}
}
