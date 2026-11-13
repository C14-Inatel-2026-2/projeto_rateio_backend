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
                                                       Pagamento (apenas domínio)
```

**Exemplo:** Ana paga R$ 90,00 de um jantar dividido igualmente com Bruno e Carla.

| Membro | Pagou | Parcela | Saldo |
|---|---:|---:|---:|
| Ana | 90,00 | 30,00 | +60,00 |
| Bruno | 0,00 | 30,00 | -30,00 |
| Carla | 0,00 | 30,00 | -30,00 |

O plano de quitação tem duas transferências: Bruno → Ana (R$ 30,00) e Carla → Ana (R$ 30,00).

**Estado atual:** membros, grupos e despesas são gravados no MySQL e expostos por HTTP; saldos
e quitação são calculados a cada consulta. Pagamentos entre membros existem apenas como regra
de negócio, sem rota nem tabela, e ainda não há autenticação por sessão ou token (ver
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

Os pacotes são organizados por domínio. `despesa` e `grupo` são divididos ainda por tipo de
arquivo, e os testes espelham essa estrutura:

```text
src/main/java/rateio
├── RateioApplication.java
├── config/          CORS, encoder de senha e tratador global de erros
├── membro/          cadastro e login
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
├── saldo/           cálculo do saldo de cada membro
├── quitacao/        plano de transferências
└── pagamento/       regra de negócio de pagamentos (sem persistência)
```

### Modelo de dados

| Tabela | Conteúdo |
|---|---|
| `membro` | nome, e-mail (único) e hash da senha |
| `grupo` | nome, criador e data de criação |
| `grupo_membro` | participação de um membro em um grupo (par único) |
| `despesa` | grupo, pagador, quem registrou, descrição, valor total, estratégia, status (`ATIVA` ou `ESTORNADA`) e datas de criação e estorno |
| `parcela` | quanto cada membro deve em uma despesa (par despesa e membro único) |

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
| 400 | Entrada inválida, parâmetro ausente, UUID malformado ou corpo ilegível |
| 401 | E-mail ou senha incorretos |
| 404 | Grupo, membro ou despesa inexistente, ou grupo do qual quem chama não participa |
| 409 | E-mail já cadastrado, membro repetido, grupo que ficaria com menos de 2 membros, remoção de membro em grupo com despesas, despesa já estornada |

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
| RF07 | Registrar pagamentos entre membros | A regra existe (pagador e recebedor diferentes, valor positivo), mas não há rota nem tabela, e o saldo ainda não considera pagamentos. | 🟡 | `Pagamento` |
| RF08 | Estornar despesas e pagamentos | O estorno é lógico: o lançamento permanece no histórico e não pode ser estornado duas vezes. Disponível por HTTP apenas para despesas. | 🟡 | `DespesaService`, `Despesa`, `Pagamento` |
| RF09 | Calcular o saldo de cada membro | Considera apenas despesas ativas. Positivo é credor, negativo é devedor, e membros sem movimentação aparecem com zero. | ✅ | `SaldoService`, `Saldo` |
| RF10 | Gerar o plano de quitação | Cruza o maior credor com o maior devedor até zerar os saldos. Rejeita saldos cuja soma não seja zero. | ✅ | `QuitacaoService`, `Quitacao` |
| RF11 | Validar o pertencimento ao grupo | Quem chama, o pagador e os participantes de uma despesa devem pertencer ao grupo. | ✅ | `AcessoAoGrupo`, `DespesaService` |
| RF12 | Expor as funcionalidades por API HTTP | Todas as funcionalidades acima têm rota, exceto pagamentos. | 🟡 | controllers |
| RF13 | Persistir os dados | Membros, grupos, participações, despesas e parcelas são gravados; pagamentos não. | 🟡 | repositórios |

## Requisitos não funcionais

| ID | Categoria | Requisito | Situação |
|---|---|---|---|
| RNF01 | Plataforma | O backend deve usar Java 21, Spring Boot 4.1.1 e Maven. | ✅ |
| RNF02 | Persistência | Os dados devem ser armazenados em MySQL por meio de Spring Data JPA. Pagamentos ainda não são gravados. | 🟡 |
| RNF03 | Precisão | Valores monetários devem usar `BigDecimal`, nunca ponto flutuante. As divisões são feitas em centavos inteiros, para que a soma das parcelas feche com o total. | ✅ |
| RNF04 | Segurança | Credenciais do banco e do Docker Hub não devem ser versionadas: vêm de variáveis de ambiente e de credenciais do Jenkins. Senhas de membros são gravadas com hash BCrypt. | ✅ |
| RNF05 | Rastreabilidade | Lançamentos não devem ser apagados ao serem estornados, e as datas de registro e de estorno devem ser guardadas. | ✅ |
| RNF06 | Integridade | Objetos inválidos devem ser rejeitados na criação, e as listas retornadas não devem permitir alterar o estado interno. | ✅ |
| RNF07 | Testabilidade | As regras de negócio e os serviços devem ter testes unitários automatizados, executáveis sem banco de dados. | ✅ |
| RNF08 | Integração contínua | O Jenkins deve gerar o JAR, rodar os testes e publicar o relatório JUnit; na `main`, deve publicar a imagem Docker da aplicação. | ✅ |
| RNF09 | Manutenibilidade | O código deve ser organizado em pacotes por domínio, separados por tipo de arquivo, e o versionamento deve seguir o [`CONVENTIONS.md`](CONVENTIONS.md). Os pacotes `membro`, `saldo` e `quitacao` ainda não foram divididos por tipo. | 🟡 |
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
| `rateio.membro.service.MembroServiceTest` | 11 |
| `rateio.pagamento.PagamentoTest` | 22 |
| `rateio.quitacao.QuitacaoServiceTest` | 6 |
| `rateio.quitacao.QuitacaoTest` | 4 |
| `rateio.quitacao.TesteQuitacao` | 4 |
| `rateio.saldo.SaldoServiceTest` | 9 |
| `rateio.saldo.SaldoTest` | 22 |
| **Total** | **204** |

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
