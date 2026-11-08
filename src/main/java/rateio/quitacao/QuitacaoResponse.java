package rateio.quitacao;

import java.util.UUID;

public record QuitacaoResponse(UUID deId, String deNome, UUID paraId, String paraNome, String valor) {

    public static QuitacaoResponse de(QuitacaoDetalhe detalhe) {
        return new QuitacaoResponse(
                detalhe.deId(),
                detalhe.deNome(),
                detalhe.paraId(),
                detalhe.paraNome(),
                detalhe.valor().toPlainString());
    }
}