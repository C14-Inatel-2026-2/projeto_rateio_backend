package rateio.grupo;

import jakarta.persistence.*;
import rateio.membro.Membro;

import java.util.UUID;
import java.time.Instant;

@Entity
@Table(name = "grupo")
public class GrupoEntidade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "criador_id", nullable = false)
    private Membro criador;

    @Column(nullable = false)
    private Instant criadoEm;

    public GrupoEntidade() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Membro getCriador() { return criador; }
    public void setCriador(Membro criador) { this.criador = criador; }

    public Instant getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }

}
