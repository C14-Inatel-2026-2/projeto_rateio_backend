package rateio.grupo.dto;

import rateio.membro.MembroResponse;

import java.util.List;
import java.util.UUID;

public record GrupoResponse(UUID id, String nome, List<MembroResponse> membros) {

    public static GrupoResponse de(GrupoDetalhe detalhe) {
        return new GrupoResponse(
                detalhe.grupo().getId(),
                detalhe.grupo().getNome(),
                detalhe.membros().stream().map(MembroResponse::de).toList());
    }
}
