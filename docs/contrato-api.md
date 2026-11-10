Contratos API:








MEMBROS:



POST /membros ---------------> **OK**

Para que serve: cadastrar uma pessoa nova

Recebe:   { "nome": "Ana", "email": "ana@x.com", "senha": "123" }

Sucesso:  201  { "id": "uuid", "nome": "Ana", "email": "ana@x.com" }

Erros:    400  nome, email ou senha ausente ou em branco

            { "mensagem": "Nome, email e senha sao obrigatorios" }

       400  JSON malformado


         409  email já cadastrado

            { "mensagem": "Email ja cadastrado" }

Obs: o email é normalizado (sem espaços, minúsculo) e é único. A senha é guardada só como hash e nunca volta na resposta





POST /membros/login ---------------> **OK**

Para que serve: confirmar quem é a pessoa

Recebe:   { "email": "ana@x.com", "senha": "123" }

Sucesso:  200  { "id": "uuid", "nome": "Ana", "email": "ana@x.com" }

Erros:    401  email inexistente, senha errada, ou campo ausente ou em branco (a mesma mensagem em todos os casos)

              { "mensagem": "Email ou senha invalidos" }

         400  JSON malformado

              { "mensagem": "Corpo da requisicao invalido" }

Obs: o email é normalizado antes da busca, então "Ana@X.com" entra. O front guarda o id devolvido e o usa nas próximas rotas





&#x09					





GRUPOS:







POST /grupos?membroId=uuid ----------> **OK**

Para que serve: criar um grupo novo, com a pessoa do membroId como criadora

Recebe:   { "nome": "Viagem", "emails": \["maria@x.com", "joao@x.com"] }

Sucesso:  201  { "id": "uuid", "nome": "Viagem", "membros": \[ { "id": "uuid", "nome": "Ana", "email": "ana@x.com" }, ... ] }

Erros:    400  nome vazio ou nenhum outro membro na lista

              { "mensagem": "Nome do grupo e ao menos um membro sao obrigatorios" }

         400  algum email da lista vazio

              { "mensagem": "Emails dos membros nao podem ser vazios" }

         400  membroId ausente

              { "mensagem": "membroId e obrigatorio" }

         404  criador ou algum email sem cadastro

              { "mensagem": "Membro nao encontrado" }

         409  email repetido na lista, ou o do próprio criador

              { "mensagem": "Membro repetido no grupo" }

Quem pede: o criador, pelo ?membroId= na URL

Obs: tudo ou nada, se qualquer membro falhar, o grupo não é criado. O criador entra sozinho no grupo e não vai na lista de emails





GET /grupos?membroId=uuid -----------> **OK**

Para que serve: listar os grupos em que o membro participa

Recebe:   nada no corpo, o membroId vem na URL

Sucesso:  200  \[ { "id": "uuid", "nome": "Viagem" }, { "id": "uuid", "nome": "Restaurante" } ]

              (lista vazia \[] se o membro não tem grupos)

Erros:    400  membroId ausente

              { "mensagem": "membroId e obrigatorio" }

         404  membro não existe

              { "mensagem": "Membro nao encontrado" }

Quem pede: o próprio membro, pelo ?membroId= na URL





GET /grupos/{grupoId}?membroId=uuid -----------> **OK**

Para que serve: ver um grupo com seus membros

Recebe:   nada no corpo, o membroId vem na URL

Sucesso:  200  { "id": "uuid", "nome": "Viagem",

                "membros": \[ { "id": "uuid", "nome": "Ana", "email": "ana@x.com" } ] }

Erros:    400  membroId ausente

              { "mensagem": "membroId e obrigatorio" }

         404  grupo não existe, ou quem pede não pertence a ele

              { "mensagem": "Grupo nao encontrado" }

Quem pede: um membro do grupo, pelo ?membroId= na URL





POST /grupos/{grupoId}/membros?membroId=uuid -----------> **OK**

Para que serve: adicionar ao grupo uma pessoa que já tem cadastro, buscando por email

Recebe:   { "email": "maria@x.com" }

Sucesso:  201  { "id": "uuid", "nome": "Maria", "email": "maria@x.com" }

Erros:    400  email vazio

              { "mensagem": "Email, membroId e grupoId sao obrigatorios" }

         400  membroId ausente

              { "mensagem": "membroId e obrigatorio" }

         404  grupo não existe, ou quem pede não pertence a ele

              { "mensagem": "Grupo nao encontrado" }

         404  o email não tem cadastro

              { "mensagem": "Membro nao encontrado" }

         409  a pessoa já está no grupo

              { "mensagem": "Membro repetido no grupo" }

Quem pede: um membro do grupo, pelo ?membroId= na URL

Obs: o email é normalizado (sem espaços, minúsculo) antes da busca





DELETE /grupos/{grupoId}/membros/{alvoId}?membroId=uuid -----------> **OK** 

Para que serve: remover um membro do grupo

Recebe:   nada no corpo

Sucesso:  204  sem corpo

Erros:    400  membroId ausente

              { "mensagem": "membroId e obrigatorio" }

         404  grupo não existe, ou quem pede não pertence a ele

              { "mensagem": "Grupo nao encontrado" }

         404  o alvo não pertence ao grupo

              { "mensagem": "Membro nao encontrado" }

         409  o grupo ficaria com menos de 2 membros

              { "mensagem": "Grupo precisa de ao menos 2 membros" }

         409  grupo já tem despesas

              { "mensagem": "Grupo com despesas nao permite remover membros" }

Quem pede: um membro do grupo, pelo ?membroId= na URL

Obs: o alvoId pode ser o próprio membroId (a pessoa saindo do grupo)



























DESPESAS:





POST /grupos/{grupoId}/despesas?membroId=uuid ---------------> **OK**

Para que serve: registrar uma despesa dizendo quem pagou e como dividir

Recebe:   { "descricao": "Jantar", "valorTotal": "100.00", "pagadorId": "uuid",

           "estrategia": "IGUAL",

           "participantes": \[ { "membroId": "uuid" }, { "membroId": "uuid" }, { "membroId": "uuid" } ] }

Sucesso:  201  { "id": "uuid", "descricao": "Jantar", "valorTotal": "100.00", "pagadorId": "uuid",

                "estrategia": "IGUAL", "status": "ATIVA", "data": "2026-11-04T20:15:00.123456Z",

                "parcelas": \[ { "membroId": "uuid", "valor": "33.34" },

                              { "membroId": "uuid", "valor": "33.33" },

                              { "membroId": "uuid", "valor": "33.33" } ] }

Erros:    400  campo vazio, valor com mais de 2 casas decimais, peso ou valor do participante nulo ou não positivo

              { "mensagem": "Valor invalido" }

         400  soma das parcelas diferente do total (valores fixos)

              { "mensagem": "Soma das parcelas diferente do total" }

         400  pagador ou participante fora do grupo

              { "mensagem": "Pagador fora do grupo" } ou { "mensagem": "Participante fora do grupo" }

         400  mesmo participante duas vezes

              { "mensagem": "Participante repetido" }

         400  estrategia fora do enum, JSON malformado ou valor não numérico

              { "mensagem": "Corpo da requisicao invalido" }

         400  membroId ausente

              { "mensagem": "membroId e obrigatorio" }

         404  grupo não existe, ou quem pede não pertence a ele

              { "mensagem": "Grupo nao encontrado" }

Quem pede: um membro do grupo, pelo ?membroId= na URL

Obs: estrategia aceita IGUAL, POR\_PESO ou VALORES\_FIXOS. Em POR\_PESO cada participante leva "peso": "2" (aceita decimais, ex.: "0.5"),

    em VALORES\_FIXOS cada um leva "valor": "30.00", e em IGUAL nenhum dos dois.

    A sobra de centavos vai 1 por vez para quem tem o maior resto da divisão, e em empate vale a ordem informada.

    Quem pede é checado antes do pagador e dos participantes (fora do grupo dá 404, nunca 400).

    Tudo ou nada: se algo falhar, nada é gravado





GET /grupos/{grupoId}/despesas?membroId=uuid ---------------> **OK**

Para que serve: listar as despesas do grupo, inclusive as estornadas

Recebe:   nada no corpo, o membroId vem na URL

Sucesso:  200  \[ { "id": "uuid", "descricao": "Jantar", "valorTotal": "100.00",

                  "pagadorId": "uuid", "status": "ATIVA", "data": "2026-11-04T20:15:00.123456Z" } ]

              (lista vazia \[] se o grupo não tem despesas)

Erros:    400  membroId ausente

              { "mensagem": "membroId e obrigatorio" }

         404  grupo não existe, ou quem pede não pertence a ele

              { "mensagem": "Grupo nao encontrado" }

Quem pede: um membro do grupo, pelo ?membroId= na URL

Obs: a ordem não é garantida, o front ordena por "data" se precisar. A lista não traz estrategia nem parcelas





POST /grupos/{grupoId}/despesas/{despesaId}/estorno?membroId=uuid ---------------> **OK**

Para que serve: desfazer uma despesa sem apagá-la, marcando como estornada

Recebe:   nada no corpo

Sucesso:  200  { "id": "uuid", "status": "ESTORNADA" }

Erros:    400  membroId ausente

              { "mensagem": "membroId e obrigatorio" }

         404  grupo ou despesa não encontrados, ou quem pede não pertence ao grupo

              { "mensagem": "Despesa nao encontrada" }

         409  despesa já estornada

              { "mensagem": "Despesa ja estornada" }

Quem pede: um membro do grupo, pelo ?membroId= na URL

Obs: é POST e não DELETE porque a despesa continua no histórico, só muda de status. Os saldos são recalculados na próxima consulta

































SALDOS E QUITAÇÃO:







GET /grupos/{grupoId}/saldos?membroId=uuid ---------------> **OK**

Para que serve: ver quanto cada membro deve ou tem a receber no grupo

Recebe:   nada no corpo, o membroId vem na URL

Sucesso:  200  \[ { "membroId": "uuid", "nome": "Ana", "saldo": "60.00" },

                { "membroId": "uuid", "nome": "Bia", "saldo": "-20.00" },

                { "membroId": "uuid", "nome": "Caio", "saldo": "-40.00" } ]

Erros:    400  membroId ausente

              { "mensagem": "membroId e obrigatorio" }

         404  grupo não existe, ou quem pede não pertence a ele

              { "mensagem": "Grupo nao encontrado" }

Quem pede: um membro do grupo, pelo ?membroId= na URL

Obs: positivo é a receber e negativo é a pagar. A soma é sempre zero.

    Todos os membros do grupo aparecem, e quem não teve movimentação vem com "0.00" (grupo sem despesas não é erro).

    Ordem: alfabética por nome (o id desempata), então a lista sai sempre na mesma ordem.

    Despesas estornadas não entram na conta. Os pagamentos passam a entrar na conta quando a fatia de pagamentos for entregue





GET /grupos/{grupoId}/quitacao?membroId=uuid ---------------> **OK**

Para que serve: sugerir a menor lista de transferências que zera as dívidas do grupo

Recebe:   nada no corpo, o membroId vem na URL

Sucesso:  200  \[ { "deId": "uuid", "deNome": "Caio", "paraId": "uuid", "paraNome": "Ana", "valor": "40.00" },

                { "deId": "uuid", "deNome": "Bia", "paraId": "uuid", "paraNome": "Ana", "valor": "20.00" } ]

              (lista vazia \[] se todos já estão quitados)

Erros:    400  membroId ausente

              { "mensagem": "membroId e obrigatorio" }

         404  grupo não existe, ou quem pede não pertence a ele

              { "mensagem": "Grupo nao encontrado" }

Quem pede: um membro do grupo, pelo ?membroId= na URL

Obs: é só uma sugestão, a rota não grava nada. Quem tem saldo zero não aparece em nenhuma transferência.

    Cada transferência liga o maior devedor ao maior credor restante, e a lista começa pelo maior devedor.

    A mesma entrada gera sempre a mesma ordem. Aplicando todas as transferências, todos os saldos ficam zero.

    Para efetivar uma transferência, o front usa a rota de registrar pagamento































PAGAMENTOS:





POST /grupos/{grupoId}/pagamentos?membroId=uuid ---------------> **OK**

Para que serve: registrar que o membro pagou uma quantia a outro membro do grupo

Recebe:   { "paraId": "uuid", "valor": "20.00" }

Sucesso:  201  { "id": "uuid", "deId": "uuid", "paraId": "uuid", "valor": "20.00",

                "status": "ATIVO", "data": "2026-11-04T21:00:00.123456Z" }

Erros:    400  valor zero, negativo, com mais de 2 casas decimais, ou pagar a si mesmo

              { "mensagem": "Valor invalido" }

         400  valor maior que a dívida, ou sem dívida com esse destinatário

              { "mensagem": "Valor maior que a divida" }

         400  campo ausente

              { "mensagem": "grupoId, registradorId, paraId e valor sao obrigatorios" }

         400  JSON malformado ou valor não numérico

              { "mensagem": "Corpo da requisicao invalido" }

         400  membroId ausente

              { "mensagem": "membroId e obrigatorio" }

         404  grupo não existe, ou quem pede não pertence a ele

              { "mensagem": "Grupo nao encontrado" }

         404  destinatário não pertence ao grupo

              { "mensagem": "Membro nao encontrado" }

Quem pede: quem paga, pelo ?membroId= na URL

Obs: não existe campo de quem paga no corpo, porque é sempre o membroId da URL.

    Dívida = a transferência que a rota de quitação sugere de quem paga para o destinatário.

    O valor não pode passar dela, e só existe pagamento entre pares que estão no plano de quitação.

    Pagamentos parciais são aceitos, e a quitação é recalculada depois de cada um.

    A ordem das validações é: valor, grupo e quem pede, destinatário e dívida





POST /grupos/{grupoId}/pagamentos/{pagamentoId}/estorno?membroId=uuid ---------------> **OK**

Para que serve: desfazer um pagamento sem apagá-lo, marcando como estornado

Recebe:   nada no corpo

Sucesso:  200  { "id": "uuid", "status": "ESTORNADO" }

Erros:    400  membroId ausente

              { "mensagem": "membroId e obrigatorio" }

         404  grupo ou pagamento não encontrados, ou quem pede não pertence ao grupo

              { "mensagem": "Pagamento nao encontrado" }

         409  pagamento já estornado

              { "mensagem": "Pagamento ja estornado" }

Quem pede: um membro do grupo, pelo ?membroId= na URL

Obs: o pagamento permanece no extrato com o status ESTORNADO. Os saldos e a quitação deixam de considerá-lo





GET /grupos/{grupoId}/extrato?membroId=uuid ---------------> **OK**

Para que serve: ver o histórico do grupo, com despesas e pagamentos em ordem cronológica

Recebe:   nada no corpo, o membroId vem na URL

Sucesso:  200  \[ { "tipo": "DESPESA", "id": "uuid", "autorId": "uuid", "autorNome": "Ana",

                  "valor": "100.00", "status": "ATIVA", "data": "2026-11-04T20:15:00.123456Z" },

                { "tipo": "PAGAMENTO", "id": "uuid", "autorId": "uuid", "autorNome": "Bia",

                  "valor": "20.00", "status": "ATIVO", "data": "2026-11-04T21:00:00.123456Z" } ]

              (lista vazia \[] se não há movimentações)

Erros:    400  membroId ausente

              { "mensagem": "membroId e obrigatorio" }

         404  grupo não existe, ou quem pede não pertence a ele

              { "mensagem": "Grupo nao encontrado" }

Quem pede: um membro do grupo, pelo ?membroId= na URL

Obs: tipo é DESPESA ou PAGAMENTO. O status de despesa é ATIVA ou ESTORNADA, e o de pagamento é ATIVO ou ESTORNADO.

    Autor da despesa = quem pagou. Autor do pagamento = quem pagou ao colega.

    Ordem cronológica pela data, com o id desempatando datas iguais. Estornados continuam na lista, marcados no status.

    É só consulta, não vira tabela









