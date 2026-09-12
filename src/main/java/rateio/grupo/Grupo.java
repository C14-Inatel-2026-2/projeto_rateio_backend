package rateio.grupo;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class Grupo {
    private UUID id;
    private String nome;
    private Set<UUID> membros;
    private Instant criadoEm;

    public Grupo(String nome, Set<UUID> membrosIniciais) {
        throw new UnsupportedOperationException("H1 - nao implementado");
    }

    /** H1: lanca conflito se o membro ja pertence ao grupo. */
    public void adicionarMembro(UUID membroId) {
        throw new UnsupportedOperationException("H1 - nao implementado");
    }

    /** H1: lanca erro se o membro nao pertence ao grupo. */
    public void removerMembro(UUID membroId) {
        throw new UnsupportedOperationException("H1 - nao implementado");
    }

    /** H2: usado para rejeitar pagador ou participante externo ao grupo. */
    public boolean contem(UUID membroId) {
        throw new UnsupportedOperationException("H1 - nao implementado");
    }

    /** H2: valida que todos os participantes de uma despesa pertencem ao grupo. */
    public boolean contemTodos(Set<UUID> membrosIds) {
        throw new UnsupportedOperationException("H1 - nao implementado");
    }

    public int quantidadeDeMembros() {
        throw new UnsupportedOperationException("H1 - nao implementado");
    }

    public UUID id() {
        throw new UnsupportedOperationException("H1 - nao implementado");
    }

    public String nome() {
        throw new UnsupportedOperationException("H1 - nao implementado");
    }

    /** Retorna copia imutavel: a colecao interna nao vaza. */
    public Set<UUID> membros() {
        throw new UnsupportedOperationException("H1 - nao implementado");
    }

    public Instant criadoEm() {
        throw new UnsupportedOperationException("H1 - nao implementado");
    }
}
