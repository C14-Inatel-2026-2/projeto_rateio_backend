package rateio.grupo;

import java.util.UUID;

public record GrupoResumoResponse(UUID id, String nome) {

    public static GrupoResumoResponse de(GrupoEntidade grupo) {
        return new GrupoResumoResponse(grupo.getId(), grupo.getNome());
    }
}
