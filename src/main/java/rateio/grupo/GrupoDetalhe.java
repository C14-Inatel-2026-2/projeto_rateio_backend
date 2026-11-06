package rateio.grupo;

import rateio.membro.Membro;

import java.util.List;

public record GrupoDetalhe(GrupoEntidade grupo, List<Membro> membros) {
}
