# INATEL_C14_PROJETO_BACKEND

Backend do **Rateio**, projeto da disciplina C14 do Inatel.

## Índice

- [Descrição do projeto](#descrição-do-projeto)
- [Arquitetura](#arquitetura)
- [API HTTP](#api-http)
- [Requisitos funcionais](#requisitos-funcionais)
- [Requisitos não funcionais](#requisitos-não-funcionais)
- [Como testar](#como-testar)
- [Como executar](#como-executar)
- [Integração e entrega contínuas](#integração-e-entrega-contínuas)
- [Tecnologias e ferramentas](#tecnologias-e-ferramentas)
- [Uso de IA](#uso-de-ia)

## Descrição do projeto

O Rateio é uma API REST que divide despesas entre os membros de um grupo: registra quem pagou
o quê, calcula quanto cada pessoa deve ou tem a receber e sugere as transferências para quitar
as dívidas.

```text
Membro → Grupo (participações) → Despesa (parcelas) → Saldo (por membro) → Quitação (transferências)
                                                           ↑
                                                       Pagamento (abate o saldo)
```

**Exemplo:** Ana paga R$ 90,00 de um jantar dividido igualmente com Bruno e Carla.

| Membro | Pagou | Parcela | Saldo |
|---|---:|---:|---:|
| Ana | 90,00 | 30,00 | +60,00 |
| Bruno | 0,00 | 30,00 | -30,00 |
| Carla | 0,00 | 30,00 | -30,00 |

O plano de quitação tem duas transferências: Bruno → Ana (R$ 30,00) e Carla → Ana (R$ 30,00).

**Estado atual:** membros, grupos, despesas e pagamentos são gravados no MySQL e expostos por
HTTP; saldos e quitação são calculados a cada consulta, considerando as despesas e os
pagamentos ativos. Ainda não há autenticação por sessão ou token (ver
[Identificação de quem chama](#identificação-de-quem-chama)).

## Arquitetura

Cada requisição atravessa quatro camadas:

1. **Controller:** recebe o HTTP, chama o serviço e converte o resultado em um record
   `...Response`. Valores monetários saem como texto (`"33.34"`) para não perder precisão no JSON.
2. **Service:** valida a entrada, confere o acesso ao grupo, consulta os repositórios e grava,
   sempre dentro de uma transação.
3. **Domínio** (`Despesa`, `Grupo`, `Saldo`, `Quitacao`, `Pagamento`): classes Java puras com as
   regras de negócio, sem Spring nem banco.
4. **Entity e Repository:** as classes `...Entidade` (e `Membro`) mapeiam as tabelas; os
   repositórios são interfaces do Spring Data.

Os pacotes são organizados por domínio e divididos por tipo de arquivo. Os testes espelham essa
estrutura:

```text
src/main/java/rateio
├── RateioApplication.java
├── config/          CorsConfig, SegurancaConfig (encoder de senha), ErroResponse e tratador global de erros
├── membro/
│   ├── controller/  MembroController
│   ├── service/     MembroService
│   ├── entity/      Membro
│   ├── repository/  MembroRepository
│   ├── dto/         CadastroRequest, LoginRequest, MembroResponse
│   └── exception/   exceções do membro
├── grupo/
│   ├── controller/  GrupoController
│   ├── service/     GrupoService, AcessoAoGrupo
│   ├── domain/      Grupo
│   ├── entity/      GrupoEntidade, GrupoMembroEntidade
│   ├── repository/  GrupoRepository, GrupoMembroRepository
│   ├── dto/         requests, responses e records de detalhe
│   └── exception/   exceções do grupo
├── despesa/
│   ├── controller/  DespesaController
│   ├── service/     DespesaService, DivisaoService
│   ├── domain/      Despesa, StatusDespesa
│   ├── entity/      DespesaEntidade, ParcelaEntidade
│   ├── repository/  DespesaRepository, ParcelaRepository
│   ├── dto/         requests, responses e records de detalhe
│   └── exception/   exceções da despesa
├── pagamento/
│   ├── controller/  PagamentoController
│   ├── service/     PagamentoService
│   ├── domain/      Pagamento, StatusPagamento, TipoMovimentacao
│   ├── entity/      PagamentoEntidade
│   ├── repository/  PagamentoRepository
│   ├── dto/         requests, responses e records de detalhe
│   └── exception/   exceções do pagamento
├── saldo/
│   ├── controller/  SaldoController
│   ├── service/     SaldoService
│   ├── domain/      Saldo
│   └── dto/         SaldoDetalhe, SaldoResponse
└── quitacao/
    ├── controller/  QuitacaoController
    ├── service/     QuitacaoService
    ├── domain/      Quitacao
    └── dto/         QuitacaoDetalhe, QuitacaoResponse
```

### Modelo de dados

| Tabela | Conteúdo |
|---|---|
| `membro` | nome, e-mail (único) e hash da senha |
| `grupo` | nome, criador e data de criação |
| `grupo_membro` | participação de um membro em um grupo (par único) |
| `despesa` | grupo, pagador, quem registrou, descrição, valor total, estratégia, status (`ATIVA` ou `ESTORNADA`) e datas de criação e estorno |
| `parcela` | quanto cada membro deve em uma despesa (par despesa e membro único) |
| `pagamento` | grupo, quem pagou (`de`), quem recebeu (`para`), valor, status (`ATIVO` ou `ESTORNADO`) e datas de criação e estorno |

Os identificadores são UUID. Saldos e planos de quitação não são gravados.

## API HTTP

| Método e caminho | Descrição | Sucesso |
|---|---|---|
| `POST /membros` | Cadastra um membro. Corpo: `nome`, `email`, `senha`. | 201 |
| `POST /membros/login` | Confere e-mail e senha e devolve o membro. | 200 |
| `POST /grupos` | Cria um grupo. Corpo: `nome`, `emails` (dos convidados). | 201 |
| `GET /grupos` | Lista os grupos do membro. | 200 |
| `GET /grupos/{grupoId}` | Detalha o grupo e seus membros. | 200 |
| `POST /grupos/{grupoId}/membros` | Adiciona um membro pelo e-mail. Corpo: `email`. | 201 |
| `DELETE /grupos/{grupoId}/membros/{alvoId}` | Remove um membro do grupo. | 204 |
| `POST /grupos/{grupoId}/despesas` | Registra uma despesa e calcula as parcelas. | 201 |
| `GET /grupos/{grupoId}/despesas` | Lista as despesas do grupo, ativas e estornadas. | 200 |
| `POST /grupos/{grupoId}/despesas/{despesaId}/estorno` | Estorna uma despesa. | 200 |
| `GET /grupos/{grupoId}/saldos` | Saldo de cada membro do grupo. | 200 |
| `GET /grupos/{grupoId}/quitacao` | Transferências sugeridas para zerar os saldos. | 200 |
| `POST /grupos/{grupoId}/pagamentos` | Registra um pagamento de quem chama para outro membro. Corpo: `paraId`, `valor`. | 201 |
| `POST /grupos/{grupoId}/pagamentos/{pagamentoId}/estorno` | Estorna um pagamento. | 200 |
| `GET /grupos/{grupoId}/extrato` | Lista as despesas e os pagamentos do grupo em ordem cronológica, ativos e estornados. | 200 |

Corpo da criação de despesa (`peso` só é usado em `POR_PESO` e `valor` só em `VALORES_FIXOS`):

```json
{
  "descricao": "Jantar",
  "valorTotal": 90.00,
  "pagadorId": "<uuid>",
  "estrategia": "IGUAL",
  "participantes": [
    { "membroId": "<uuid>", "peso": 1, "valor": 30.00 }
  ]
}
```

### Identificação de quem chama

Com exceção das rotas de `/membros`, todas exigem o parâmetro `?membroId=<uuid>` de quem faz a
chamada. Não há sessão nem token: o login apenas devolve os dados do membro, e o `membroId` é
aceito sem prova de identidade.

Quem não participa de um grupo recebe a mesma resposta de grupo inexistente (404), para não
revelar a existência de grupos alheios.

### Erros

Os erros são devolvidos como `{"mensagem": "..."}`:

| Status | Quando |
|---|---|
| 400 | Entrada inválida, parâmetro ausente, UUID malformado, corpo ilegível, valor de pagamento inválido ou maior que a dívida |
| 401 | E-mail ou senha incorretos |
| 404 | Grupo, membro, despesa ou pagamento inexistente, ou grupo do qual quem chama não participa |
| 409 | E-mail já cadastrado, membro repetido, grupo que ficaria com menos de 2 membros, remoção de membro em grupo com despesas, despesa ou pagamento já estornado |

## Requisitos funcionais

**Situação:** ✅ implementado · 🟡 parcial · ⬜ pendente

| ID | Requisito | Regras principais | Situação | Onde |
|---|---|---|---|---|
| RF01 | Cadastrar membros e autenticar | Nome, e-mail e senha obrigatórios. O e-mail é normalizado e único; a senha é gravada com BCrypt. O login não diferencia e-mail inexistente de senha errada. | ✅ | `MembroService` |
| RF02 | Criar grupos | Exige nome e ao menos um convidado já cadastrado. O criador entra automaticamente; e-mail repetido ou do próprio criador é rejeitado. | ✅ | `GrupoService` |
| RF03 | Gerenciar membros do grupo | Rejeita adicionar quem já participa e remover quem não participa. O grupo não pode ficar com menos de 2 membros, e ninguém pode ser removido de um grupo que já tenha despesas. | ✅ | `GrupoService`, `Grupo` |
| RF04 | Registrar despesas | Exige descrição, valor positivo com até 2 casas, pagador, estratégia e participantes. A soma das parcelas deve ser igual ao total, sem membro repetido. | ✅ | `DespesaService`, `Despesa` |
| RF05 | Dividir despesas igualmente | Os centavos restantes vão, um por vez, para os primeiros participantes da lista. | ✅ | `DivisaoService` |
| RF06 | Dividir despesas por peso ou por valores fixos | `POR_PESO` usa o método do maior resto para distribuir os centavos; `VALORES_FIXOS` exige que os valores informados somem o total. | ✅ | `DivisaoService` |
| RF07 | Registrar pagamentos entre membros | Pagador e recebedor diferentes, ambos do grupo; valor positivo com até 2 casas. O pagamento deve corresponder a uma transferência do plano de quitação e não pode passar do valor devido. | ✅ | `PagamentoService`, `Pagamento` |
| RF08 | Estornar despesas e pagamentos | O estorno é lógico: o lançamento permanece no histórico e não pode ser estornado duas vezes. Disponível por HTTP para despesas e pagamentos. | ✅ | `DespesaService`, `Despesa`, `PagamentoService`, `Pagamento` |
| RF09 | Calcular o saldo de cada membro | Considera as despesas e os pagamentos ativos. Positivo é credor, negativo é devedor, e membros sem movimentação aparecem com zero. | ✅ | `SaldoService`, `Saldo` |
| RF10 | Gerar o plano de quitação | Cruza o maior credor com o maior devedor até zerar os saldos. Rejeita saldos cuja soma não seja zero. | ✅ | `QuitacaoService`, `Quitacao` |
| RF11 | Validar o pertencimento ao grupo | Quem chama, o pagador e os participantes de uma despesa devem pertencer ao grupo. | ✅ | `AcessoAoGrupo`, `DespesaService` |
| RF12 | Expor as funcionalidades por API HTTP | Todas as funcionalidades acima têm rota. | ✅ | controllers |
| RF13 | Persistir os dados | Membros, grupos, participações, despesas, parcelas e pagamentos são gravados. | ✅ | repositórios |
| RF14 | Consultar o extrato do grupo | Lista despesas e pagamentos do grupo, ativos e estornados, em ordem cronológica. Quem chama deve participar do grupo. | ✅ | `PagamentoService` |

## Requisitos não funcionais

| ID | Categoria | Requisito | Situação |
|---|---|---|---|
| RNF01 | Plataforma | O backend deve usar Java 21, Spring Boot 4.1.1 e Maven. | ✅ |
| RNF02 | Persistência | Os dados devem ser armazenados em MySQL por meio de Spring Data JPA. | ✅ |
| RNF03 | Precisão | Valores monetários devem usar `BigDecimal`, nunca ponto flutuante. As divisões são feitas em centavos inteiros, para que a soma das parcelas feche com o total. | ✅ |
| RNF04 | Segurança | Credenciais do banco e do Docker Hub não devem ser versionadas: vêm de variáveis de ambiente e de credenciais do Jenkins. Senhas de membros são gravadas com hash BCrypt. | ✅ |
| RNF05 | Rastreabilidade | Lançamentos não devem ser apagados ao serem estornados, e as datas de registro e de estorno devem ser guardadas. | ✅ |
| RNF06 | Integridade | Objetos inválidos devem ser rejeitados na criação, e as listas retornadas não devem permitir alterar o estado interno. | ✅ |
| RNF07 | Testabilidade | As regras de negócio e os serviços devem ter testes unitários automatizados, executáveis sem banco de dados. | ✅ |
| RNF08 | Integração contínua | O Jenkins deve gerar o JAR, rodar os testes e publicar o relatório JUnit; na `main`, deve publicar a imagem Docker da aplicação. | ✅ |
| RNF09 | Manutenibilidade | O código deve ser organizado em pacotes por domínio, separados por tipo de arquivo, e o versionamento deve seguir o [`CONVENTIONS.md`](CONVENTIONS.md). | ✅ |
| RNF10 | Privacidade | Quem não participa de um grupo não deve conseguir distinguir um grupo alheio de um inexistente. | ✅ |
| RNF11 | Autenticação | As rotas devem identificar quem chama de forma verificável. Hoje o `membroId` é aceito sem prova de identidade. | ⬜ |

## Como testar

**Pré-requisitos:** JDK 21 ou superior e Maven 3.9 ou superior. O repositório não inclui
Maven Wrapper. Os testes são unitários (os serviços usam repositórios simulados com Mockito),
portanto **não precisam de MySQL** nem de variáveis de ambiente.

```bash
# Todos os testes
mvn test

# Uma classe específica
mvn test -Dtest=SaldoTest

# Um único método
mvn test -Dtest=GrupoTest#contemNegativoTest
```

Os relatórios ficam em `target/surefire-reports/`, e o de cobertura do JaCoCo em
`target/site/jacoco/`.

| Classe de teste | Testes |
|---|---:|
| `rateio.despesa.domain.DespesaTest` | 23 |
| `rateio.despesa.service.DespesaServiceTest` | 42 |
| `rateio.despesa.service.DivisaoServiceTest` | 18 |
| `rateio.grupo.domain.GrupoTest` | 5 |
| `rateio.grupo.service.GrupoServiceTest` | 38 |
| `rateio.membro.service.MembroServiceTest` | 10 |
| `rateio.pagamento.domain.PagamentoTest` | 22 |
| `rateio.pagamento.service.PagamentoServiceTest` | 31 |
| `rateio.quitacao.domain.QuitacaoTest` | 6 |
| `rateio.quitacao.service.QuitacaoServiceTest` | 6 |
| `rateio.saldo.domain.SaldoTest` | 22 |
| `rateio.saldo.service.SaldoServiceTest` | 11 |
| **Total** | **234** |

Não há testes de controller nem de integração com o MySQL.

## Como executar

A aplicação precisa de um MySQL acessível e sobe na porta 8080. O CORS aceita apenas o
front-end em `http://localhost:3000`.

1. Crie o banco:

   ```sql
   CREATE DATABASE rateio;
   ```

2. Defina as variáveis de ambiente:

   | Variável | Obrigatória | Padrão |
      |---|---|---|
   | `DB_URL` | Não | `jdbc:mysql://localhost:3306/rateio` |
   | `DB_USER` | Sim | |
   | `DB_PASSWORD` | Sim | |

3. Execute pelo JAR:

   ```bash
   mvn -B -DskipTests clean package
   java -jar target/backend-1.0-SNAPSHOT.jar
   ```

   ou pela imagem publicada no Docker Hub:

   ```bash
   docker run -p 8080:8080 \
     -e DB_URL=jdbc:mysql://<host>:3306/rateio -e DB_USER=<usuario> -e DB_PASSWORD=<senha> \
     0vat5ug/c14-rateio:latest
   ```

   Dentro do contêiner, `localhost` aponta para o próprio contêiner, e não para a máquina; por
   isso `DB_URL` precisa indicar o endereço do MySQL.

Com `spring.jpa.hibernate.ddl-auto=update`, o Hibernate cria ou atualiza as tabelas ao
iniciar; não há scripts de migração.

## Integração e entrega contínuas

O `Jenkinsfile` define três estágios, executados em agente Windows com as ferramentas `jdk21`
e `maven` configuradas no Jenkins:

| Estágio | Comando | Resultado |
|---|---|---|
| Build | `mvn -B -DskipTests clean package` | Gera o JAR e o arquiva no Jenkins. |
| Test | `mvn test` | Roda os testes e publica o relatório JUnit. |
| Deploy | `mvn -B -DskipTests compile jib:build -Djib.to.tags=%BUILD_NUMBER%,latest` | Publica a imagem `0vat5ug/c14-rateio` no Docker Hub com as tags `latest` e o número do build. |

- O Deploy roda apenas quando `GIT_BRANCH` é `origin/main` (job Pipeline) ou `main` (job
  Multibranch).
- A imagem é montada pelo [Jib](https://github.com/GoogleContainerTools/jib) sobre
  `eclipse-temurin:21-jre`, sem precisar de Docker instalado no agente.
- O Jib lê usuário e token do Docker Hub de `DOCKERHUB_USR` e `DOCKERHUB_PSW`, que o Jenkins
  cria a partir de uma credencial do tipo *Username with password* com id **`dockerhub`**. Essa
  credencial precisa existir antes do primeiro deploy.

Para montar a imagem localmente sem publicá-la:

```bash
mvn -B -DskipTests compile jib:buildTar
```

O arquivo é gerado em `target/jib-image.tar` e pode ser carregado com `docker load`.

## Tecnologias e ferramentas

| Categoria | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1.1 (Spring MVC e Spring Data JPA) |
| Banco de dados | MySQL (`mysql-connector-j`) |
| Senhas | BCrypt (`spring-security-crypto`) |
| Build | Maven |
| Testes | JUnit 5, Mockito e JaCoCo |
| Imagem de contêiner | Jib (`jib-maven-plugin`) e Docker Hub |
| CI/CD | Jenkins |
| Versionamento | Git e GitHub |
| IDE | IntelliJ IDEA |
| Assistente de IA | Claude Code |

## Uso de IA

A IA foi usada como apoio em testes, refatoração, CI/CD e documentação.

> **Escopo deste registro:** os itens abaixo vêm do histórico local do Claude Code de
> Gustavo Ferreira Vieira neste repositório, com os prompts transcritos literalmente. O uso
> de IA por outros integrantes não está coberto; cada um deve acrescentar o seu.

| Ferramenta | Modelo | Finalidade |
|---|---|---|
| Claude Code (CLI) | Claude Opus 5.5 (`claude-opus-5-5`) | Testes unitários, stage de deploy, histórias de usuário e este README |
| Claude Code (CLI) | Claude Fable 5.2 (`claude-fable-5-2`) | Refatoração de `despesa` e `grupo` e deploy no Docker Hub |

### Testes unitários da classe `Grupo` (02/10/2026)

**Prompts:**

1. > Realize 4 testes unitários (sendo 2 com mock e 2 sem mock, um dos casos de teste sendo
   > um teste negativo, ou seja, o teste deverá suceder em caso de falha) em relação à
   > classe Grupo. Caso não seja possível realizar os testes com mock nessa classe, faça-os
   > em outra que seja possível. Ferramentas utilizadas: JUnit com Mockito. Considere cada
   > teste como um commit separado, mas não faça os commits por sua conta, deixe essa parte
   > comigo

2. > siga o padrão de nomenclatura do arquivo atual baseado no novo Grupo.java e adicione
   > mais um teste negativo e os dois testes com mock

3. > adicione o ultimo teste (getDataCriacaoAposAlterarMembrosMockTest())

**Resultado:** a IA escreveu `adicionarMembroDuplicadoNegativoTest`,
`getDataCriacaoMockTest` e `getDataCriacaoAposAlterarMembrosMockTest`, hoje em
`src/test/java/rateio/grupo/domain/GrupoTest.java`. Os outros dois testes da classe
(`adicionarMembroTest` e `contemNegativoTest`) foram escritos pelo autor, que também fez os
commits (`35bd487`, `7caa873` e `87427d4`).

### Stage de deploy no Jenkins (16/10/2026)

**Prompt:**

> add a minimal and simple deploy job for this project, considering the state it is now,
> also following the good practices for Jenkins, as the Jenkinsfile suggests

**Resultado:** a IA adicionou o stage `Deploy` ao `Jenkinsfile`, restrito à branch `main`,
que entrou no histórico pelo commit `dcbc071`. Esse stage copiava o JAR para uma pasta local e
foi substituído em 11/11/2026 pela publicação no Docker Hub.

### Histórias de usuário para o Jira (31/10/2026)

**Prompts:**

1. > create 6 well defined user stories based off of this repository to be added on
   > Atlassian's Jira

2. > turn them into a CSV for Jira's import

**Resultado:** a IA redigiu seis histórias com critérios de aceite a partir do código e gerou
um CSV para importação no Jira.

### Refatoração de `despesa` e `grupo` e deploy no Docker Hub (11/11/2026)

**Prompts:**

1. > Faça uma refatoração dos arquivos em despesa/ e grupo/ (dentro de src/ e suas
   > contrapartes em test/) seguindo as boas práticas de SOLID (quando aplicáveis),
   > simplificando o funcionamento sem sacrificar funcionalidade. Além disso, crie diretórios
   > próprios para as Services, Controllers, Entidades, etc (basicamente, para dividir e
   > melhor organizar os tipos de arquivos). Após o refactor, adapte o pipeline no
   > Jenkinsfile e teste-o para garantir que continua funcionando como deveria. Quanto ao
   > pipeline, faça o deploy do backend no repositório
   > https://hub.docker.com/repository/docker/0vat5ug/c14-rateio/general hospedado no Docker
   > Hub (docker push 0vat5ug/c14-rateio:tagname). Quanto aos arquivos em si, tenha em mente
   > que comentários devem ser adicionados apenas para explicar o motivo pelo qual uma
   > decisão foi tomada, jamais para explicar elementos entendíveis apenas lendo o código;
   > assuma que quem está lendo o código conhece a linguagem e lógica de programação. Fora
   > isso, siga as boas práticas da Engenharia de Software e da linguagem Java.

**Resultado:** a IA dividiu `despesa` e `grupo` em subpacotes por tipo de arquivo, extraiu a
checagem de acesso ao grupo para `AcessoAoGrupo`, moveu a divisão por valores fixos para o
`DivisaoService` (com 5 novos testes) e adaptou os testes às novas assinaturas. No pipeline,
trocou a cópia do JAR pela publicação da imagem com o Jib, já que o agente não tem Docker, e
validou os três estágios localmente (204 testes, nenhuma falha) sem publicar a imagem. A
condição de branch do Deploy foi ajustada pelo autor. A criação da credencial `dockerhub` no
Jenkins não foi feita, porque o Jenkins local ainda não estava configurado.

### Este README (31/10/2026 e 12/11/2026)

**Prompts:**

1. > Update the README file with how the project works, how to test it, technologies/tools
   > used and a dedicated session for AI use (models used, prompts asked, for what, etc)

2. > regarding the README, rewrite it in order to it to hold the project's description,
   > functional and non-functional requirements

3. > Atualize o README de forma a se adaptar a como o projeto está agora após o refactor

**Resultado:** a IA redigiu este documento a partir do código, do `Jenkinsfile`, do `pom.xml`
e do histórico local do Claude Code. Na atualização de 12/11/2026, revisou os requisitos, a
contagem de testes (204 testes, nenhuma falha), as instruções de execução e o pipeline para
refletir o estado após a refatoração.


### Uso de IA por Cauã

> Os prompts abaixo foram resumidos e reescritos para organização; refletem o pedido feito, não o texto exato.

| Ferramenta | Modelo | Finalidade |
|---|---|---|
| Claude (chat web) | Claude Sonnet 5.5 (`claude-sonnet-5-5`) | Contrato de rotas, molde das tabelas, apoio nas rotas e desenvolvimento, configuração do Jenkins e do Railway |

**Para que foi usada:** criação do contrato de rotas a partir das histórias de usuário; molde
das tabelas do banco (lógica e ligações); apoio no desenvolvimento das classes de domínio e seus testes;
apoio no desenvolvimento das rotas e seus testes; configuração do Jenkins e do Railway 
e tarefas gerais de organização e documentação (estruturação do README,
histórias de usuário no Jira, descrições de PR, mensagens de commit e resolução de problemas
com Git).

**Dinâmica de uso:** individual, em conversa com o Claude, como um tutor. Cauã tentava
primeiro, levava a dúvida ou o erro e decidia o que aplicar. Não houve revisão de PR por IA.

#### Contrato de rotas

**Prompt:**

> Com base nestas histórias de usuário, monte o contrato das rotas da API REST: método,
> caminho, corpo, resposta de sucesso e erros possíveis.

**Resultado:** a IA propôs o contrato inicial. Cauã **ajustou** (por exemplo, o
`?membroId=` como parâmetro de consulta em todas as rotas de grupo, sem aninhar sob
`/membros`) e manteve o documento atualizado a cada fatia. A cada ajuste feito, era solicitado
uma nova versão do contrato de rotas para garantir que estaria atualizada para uso do frontend.

#### Molde das tabelas do banco

**Prompt:**

> Com base nas histórias de usuário e no contrato de rotas, desenhe as tabelas do banco e as
> ligações entre elas (chaves primárias e estrangeiras). Não precisa gerar as entidades Java.

**Resultado:** a IA sugeriu o desenho das seis tabelas (`membro`, `grupo`, `grupo_membro`,
`despesa`, `parcela`, `pagamento`) e as ligações. **Aceito com ajustes** feitos por Cauã. As
classes de entidade foram escritas por ele no IntelliJ.

#### Configuração do Jenkins e do deploy

**Prompt:**

> O stage de deploy do Jenkinsfile publica a imagem no Docker Hub e foi criado por um colega.
> Como o Jenkins roda local na minha máquina, como verifico se o deploy está funcionando?

**Resultado:** a IA explicou que a credencial `dockerhub` precisa existir no Jenkins local.
Quando o deploy falhou com `401 Unauthorized` ao baixar a imagem base, a IA identificou o
login vencido do Docker em `config.json`, e o `docker logout` resolveu (**aceito**).

#### Deploy automático no Railway

**Prompt:**

> Depois de publicar a imagem no Docker Hub, o Railway não atualizou o serviço sozinho. Como
> faço o deploy no domínio do Railway acontecer automaticamente a cada merge no repositório?

**Resultado:** a IA sugeriu o *auto update* do Railway, que foi configurado mas **não
funcionou** com a tag `latest` (**descartado**). A solução adotada foi o Jenkins executar
`railway redeploy` ao fim do stage Deploy, com um token de projeto guardado na credencial
`railway-token` (**aceito**). O push direto na `main` foi recusado pela proteção da branch;
a alteração seguiu por Pull Request.

#### Tarefas gerais

**Prompts:**

1. > Estruture o README do projeto com descrição, arquitetura, requisitos e instruções de
   > execução.

2. > Crie histórias de usuário bem definidas, com critérios de aceite, para importar no Jira.

3. > Escreva um título e uma descrição simples para o Pull Request que adiciona o redeploy no
   > Railway ao Jenkinsfile.

**Resultado:** a IA gerou a estrutura do README, as histórias de usuário e a descrição do PR.
Cauã **revisou e ajustou** o conteúdo ao estado real do projeto antes de usar. Também pediu
mensagens de commit seguindo o padrão do projeto e ajuda para resolver problemas de Git
(`cherry-pick` de um commit para outra branch e remote do
repositório que mudou de endereço)

### Uso de IA por Paulo

> Os prompts abaixo foram resumidos e reescritos para organização; refletem o pedido feito, não o texto exato.

| Ferramenta | Modelo | Finalidade |
|---|---|---|
| Gemini (chat web) | Gemini (Assistente de IA) | Configuração do ambiente e Git, divisão determinística de despesas, estruturação de testes, configuração do Jenkins/JaCoCo, refatorações de Clean Code e documentação |

**Para que foi usada:** orientação inicial para integração do IntelliJ com o GitHub e comandos de terminal Git; apoio na lógica de divisão determinística com `BigDecimal` e arredondamento seguro; suporte na criação e estruturação de testes unitários para a classe `Despesa` e o serviço de membros; configuração do pipeline de Integração Contínua (CI) no Jenkins e do plugin JaCoCo de cobertura de código; refatorações iterativas para otimização de código, extração de métodos privados, tratamento de exceções e padronização de helpers nos testes dos módulos de membros e saldos; e padronização da documentação de uso de IA no README.

**Dinâmica de uso:** individual, em conversa com o assistente como um tutor de *pair programming*. Paulo tentava primeiro, levava a dúvida, o erro ou o código para receber sugestões, aplicava as modificações no IntelliJ, executava os testes locais e validava a coerência dos resultados antes de commitar. Não houve revisão de PR por IA.

#### Configuração do ambiente e versionamento (Git)

**Prompt:**

> Como linkar o IntelliJ IDEA ao GitHub e quais são os comandos fundamentais de linha de comando do Git para controle de versão, commits atômicos e gestão de branches para quem está começando?

**Resultado:** a IA orientou o passo a passo da integração do IDE e explicou a lógica dos comandos de terminal. **Aplicado** com sucesso para ganho de autonomia no versionamento (com manutenções autônomas subsequentes no `.gitignore` e remoção da pasta `.idea`).

#### Divisão determinística (`DivisaoService`)

**Prompt:**

> Com base na regra de negócio de divisão de despesas, como estruturar a lógica de divisão exata de valores monetários utilizando BigDecimal e arredondamento seguro (RoundingMode.HALF_UP) para evitar perda de precisão?

**Resultado:** a IA orientou a estrutura inicial e a lógica de arredondamento determinístico. Paulo **implementou** o código, ajustou a suíte `DivisaoServiceTest` e validou os cálculos matemáticos.

#### Testes unitários da classe `Despesa`

**Prompt:**

> Quais cenários de testes unitários são essenciais para cobrir as regras de negócio da classe Despesa, como fechamento de parcelas, valores devidos por membro, estorno e imutabilidade de listas? Como estruturá-los?

**Resultado:** a IA sugeriu cenários robustos de teste. Paulo **escreveu e organizou** os testes no `DespesaTest` cobrindo o fechamento correto (`parcelasFecham()`), valores por participante (`parcelaDe()`), estorno e inativação (`estornar()`, `estaAtiva()`) e garantia de imutabilidade (`UnsupportedOperationException`).

#### Configuração do JaCoCo e do Jenkins (`Jenkinsfile`)

**Prompt:**

> Como configurar o plugin do JaCoCo (jacoco-maven-plugin) no pom.xml para rastreamento de cobertura e como estruturar o Jenkinsfile para executar 'mvn test jacoco:report' e publicar os relatórios JUnit e métricas de cobertura na seção post?

**Resultado:** a IA detalhou o funcionamento do Jenkins e a sintaxe correta dos blocos do JaCoCo e do script de pipeline. Paulo **configurou** o ambiente de CI, testou a execução e garantiu a geração automatizada dos relatórios e métricas.

#### Refatoração do módulo de membros (`MembroService`)

**Prompt:**

> Como posso refatorar MembroService e seus testes unitários aplicando Clean Code (como extração de métodos privados e princípio DRY) e estruturar construtores customizados nas exceções (EmailJaCadastradoException, CredenciaisInvalidasException, MembroNaoEncontradoException)?

**Resultado:** a IA sugeriu a extração da validação para `validarCamposObrigatorios`, a criação de construtores nas exceções e um método *helper* (`criarMembroPadrao()`) nos testes. Paulo **aplicou** as sugestões, ajustou detalhes conforme o *code review* do grupo (como a remoção dos construtores customizados sem uso) e validou todos os testes (100% verdes)[cite: 1, 2, 3, 4, 5].

#### Refatoração e otimização do módulo de saldos (`saldo`)

**Prompt:**

> Quais sugestões de otimização estrutural, limpeza de código e extração de métodos auxiliares posso aplicar no record Saldo, no SaldoService e nos testes (SaldoServiceTest / SaldoTest) mantendo a legibilidade aprovada pelo time (como laços for tradicionais)?

**Resultado:** a IA propôs melhorias de complexidade ciclomática e padronização de helpers. Paulo **aplicou** as refatorações de forma iterativa, executou os testes locais e garantiu a coerência do código[cite: 5].

#### Documentação do uso de IA

**Prompt:**

> Com base no meu histórico de commits e interações ao longo do projeto, estruture e padronize o relato do meu uso de Inteligência Artificial no formato do README da equipe.

**Resultado:** a IA consolidou o histórico de prompts, ações e resultados em formato Markdown padronizado. Paulo **revisou e validou** o conteúdo antes de incorporá-lo à documentação oficial do repositório.
