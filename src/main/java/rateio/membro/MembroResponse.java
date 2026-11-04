package rateio.membro;

import java.util.UUID;

public record MembroResponse(UUID id, String nome, String email) {

    public static MembroResponse de(Membro membro) {
        return new MembroResponse(membro.getId(), membro.getNome(), membro.getEmail());
    }
}
