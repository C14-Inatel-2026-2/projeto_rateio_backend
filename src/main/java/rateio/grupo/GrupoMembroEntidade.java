package rateio.grupo;

import jakarta.persistence.*;
import rateio.membro.Membro;

import java.util.UUID;

@Entity
@Table(name = "grupo_membro",
        uniqueConstraints = @UniqueConstraint(columnNames = {"grupo_id", "membro_id"}))
public class GrupoMembroEntidade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id", nullable = false)
    private GrupoEntidade grupo;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "membro_id", nullable = false)
    private Membro membro;

    public GrupoMembroEntidade() {}

    public UUID getId() {return id;}
    public void setId(UUID id) {this.id = id;}

    public GrupoEntidade getGrupoEntidade() {return grupo;}
    public void setGrupoEntidade(GrupoEntidade grupoEntidade) {this.grupo = grupoEntidade;}

    public Membro getMembro() {return membro;}
    public void setMembro(Membro membro) {this.membro = membro;}
}
