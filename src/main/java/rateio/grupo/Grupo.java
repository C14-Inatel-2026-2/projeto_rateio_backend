package rateio.grupo;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class Grupo {
    private UUID        id;
    private String      nome;
    private Set<UUID>   membros;
    private Instant     criadoEm;

    public Grupo(String nome, Set<UUID> membrosIniciais) {
        this(nome, membrosIniciais, Clock.systemUTC());
    }

    public Grupo(String nome, Set<UUID> membrosIniciais, Clock relogio) {
        this.id         = UUID.randomUUID();
        this.nome       = Objects.requireNonNull(nome, "nome");
        this.membros    = new HashSet<>(Objects.requireNonNull(membrosIniciais, "membrosIniciais"));
        this.criadoEm   = Objects.requireNonNull(relogio, "relogio").instant();
    }

    public void adicionarMembro(UUID membroId) {
        Objects.requireNonNull(membroId, "membroId");
        if (!membros.add(membroId)) {
            throw new IllegalStateException("Membro ja pertence ao grupo: " + membroId);
        }
    }

    public void removerMembro(UUID membroId) {
        if (!membros.remove(membroId)) {
            throw new IllegalArgumentException("Membro nao pertence ao grupo: " + membroId);
        }
    }

    public boolean contem(UUID membroId) {
        return membros.contains(membroId);
    }

    public boolean contemTodos(Set<UUID> membrosIds) {
        return membros.containsAll(membrosIds);
    }

    public int getQuantidadeDeMembros() {
        return membros.size();
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Set<UUID> getMembros() {
        return Set.copyOf(membros);
    }

    public Instant getDataCriacao() {
        return criadoEm;
    }
}
