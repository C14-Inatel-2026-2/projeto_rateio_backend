package rateio.grupo.entity;

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

    public GrupoMembroEntidade(GrupoEntidade grupo, Membro membro) {
        this.grupo = grupo;
        this.membro = membro;
    }

    public UUID getId() {return id;}
    public void setId(UUID id) {this.id = id;}

    public GrupoEntidade getGrupo() {return grupo;}
    public void setGrupo(GrupoEntidade grupo) {this.grupo = grupo;}

    public Membro getMembro() {return membro;}
    public void setMembro(Membro membro) {this.membro = membro;}
}
