package rateio.grupo;

import java.util.List;

public record CriarGrupoRequest(String nome, List<String> emails) {
}
