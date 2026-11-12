package rateio.grupo.dto;

import rateio.grupo.entity.GrupoEntidade;
import rateio.membro.Membro;

import java.util.List;

public record GrupoDetalhe(GrupoEntidade grupo, List<Membro> membros) {
}
