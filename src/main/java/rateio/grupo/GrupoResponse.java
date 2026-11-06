package rateio.grupo;

import rateio.membro.Membro;
import rateio.membro.MembroResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record GrupoResponse(UUID id, String nome, List<MembroResponse> membros) {

    public static GrupoResponse de(GrupoDetalhe detalhe) {
        List<MembroResponse> membros = new ArrayList<>();
        for (Membro membro : detalhe.membros()) {
            membros.add(MembroResponse.de(membro));
        }
        return new GrupoResponse(detalhe.grupo().getId(), detalhe.grupo().getNome(), membros);
    }
}