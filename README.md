# INATEL_C14_PROJETO_BACKEND

Backend do **Rateio**, projeto da disciplina C14 do Inatel.

## Índice

- [Descrição do projeto](#descrição-do-projeto)
- [Requisitos funcionais](#requisitos-funcionais)
- [Requisitos não funcionais](#requisitos-não-funcionais)
- [Como testar](#como-testar)
- [Como executar](#como-executar)
- [Tecnologias e ferramentas](#tecnologias-e-ferramentas)
- [Uso de IA](#uso-de-ia)

## Descrição do projeto

O Rateio divide despesas entre os membros de um grupo: registra quem pagou o quê, calcula
quanto cada pessoa deve ou tem a receber e sugere as transferências para quitar as dívidas.

```text
Grupo + Membros → Despesa (parcelas) → Saldo (por membro) → Quitação (transferências)
                                          ↑
                                      Pagamento
```

**Exemplo:** Ana paga R$ 90,00 de um jantar dividido igualmente com Bruno e Carla.

| Membro | Pagou | Parcela | Saldo |
|---|---:|---:|---:|
| Ana | 90,00 | 30,00 | +60,00 |
| Bruno | 0,00 | 30,00 | -30,00 |
| Carla | 0,00 | 30,00 | -30,00 |

O plano de quitação tem duas transferências: Bruno → Ana (R$ 30,00) e Carla → Ana (R$ 30,00).

**Estado atual:** o projeto é um núcleo de regras de negócio em Java. Ainda não há endpoints
HTTP, e `Membro` é a única classe gravada no banco de dados.

## Requisitos funcionais

**Situação:** ✅ implementado no código · 🟡 parcial · ⬜ pendente

| ID | Requisito | Regras principais | Situação | Classe |
|---|---|---|---|---|
| RF01 | Cadastrar membros | Cada membro tem nome e e-mail. Hoje existe apenas a entidade, sem fluxo de cadastro. | 🟡 | `Membro` |
| RF02 | Criar grupos | O grupo exige nome e lista de membros, que pode ser vazia. | ✅ | `Grupo` |
| RF03 | Gerenciar membros do grupo | Rejeita adicionar quem já pertence ao grupo e remover quem não pertence. | ✅ | `Grupo` |
| RF04 | Registrar despesas | Exige grupo, pagador, descrição, valor positivo, estratégia e parcelas. A soma das parcelas deve ser igual ao valor total, sem membro repetido. | ✅ | `Despesa` |
| RF05 | Dividir despesas igualmente | A soma das parcelas fecha com o total. Os centavos restantes vão, um por vez, para os primeiros membros da lista. | ✅ | `DivisaoService` |
| RF06 | Dividir despesas por peso ou por valores fixos | As estratégias `POR_PESO` e `VALORES_FIXOS` podem ser selecionadas, mas as parcelas ainda são informadas manualmente. | 🟡 | `Despesa` |
| RF07 | Registrar pagamentos entre membros | Exige grupo, pagador, recebedor e valor positivo. Pagador e recebedor devem ser diferentes. | ✅ | `Pagamento` |
| RF08 | Estornar despesas e pagamentos | O estorno é lógico: o lançamento permanece no histórico e não pode ser estornado duas vezes. | ✅ | `Despesa`, `Pagamento` |
| RF09 | Calcular o saldo de cada membro | Considera apenas lançamentos ativos. Positivo é credor, negativo é devedor, e membros sem movimentação aparecem com zero. | ✅ | `Saldo` |
| RF10 | Gerar o plano de quitação | Cruza o maior credor com o maior devedor até zerar os saldos. Rejeita saldos cuja soma não seja zero. | ✅ | `Quitacao` |
| RF11 | Validar o pertencimento ao grupo | Pagadores, recebedores e participantes devem pertencer ao grupo. `Grupo.contemTodos` existe, mas os lançamentos não fazem essa validação. | ⬜ | `Despesa`, `Pagamento` |
| RF12 | Expor as funcionalidades por API HTTP | O Spring MVC já é dependência do projeto, mas não há controllers. | ⬜ | |
| RF13 | Persistir grupos, despesas, pagamentos e saldos | Essas classes vivem apenas em memória. | ⬜ | |

## Requisitos não funcionais

| ID | Categoria | Requisito | Situação |
|---|---|---|---|
| RNF01 | Plataforma | O backend deve usar Java 21, Spring Boot 4.1.1 e Maven. | ✅ |
| RNF02 | Persistência | Os dados devem ser armazenados em MySQL por meio de Spring Data JPA. Hoje isso cobre apenas `Membro`. | 🟡 |
| RNF03 | Precisão | Valores monetários devem usar `BigDecimal`, nunca ponto flutuante. A divisão igual trabalha com duas casas decimais. | ✅ |
| RNF04 | Segurança | Credenciais do banco não devem ser versionadas: usuário e senha vêm de variáveis de ambiente. | ✅ |
| RNF05 | Rastreabilidade | Lançamentos não devem ser apagados ao serem estornados, e as datas de registro e de estorno devem ser guardadas. | ✅ |
| RNF06 | Integridade | Objetos inválidos devem ser rejeitados na criação, e as listas retornadas não devem permitir alterar o estado interno. | ✅ |
| RNF07 | Testabilidade | As regras de negócio devem ter testes unitários automatizados, executáveis sem banco de dados. | ✅ |
| RNF08 | Integração contínua | O Jenkins deve gerar o JAR e rodar os testes, publicando o relatório JUnit. O deploy ocorre apenas na `main`, em agente Windows, e apenas copia o JAR para `C:\deploy\rateio-backend`. | ✅ |
| RNF09 | Manutenibilidade | O código deve ser organizado em pacotes por domínio, e o versionamento deve seguir o [`CONVENTIONS.md`](CONVENTIONS.md). | ✅ |

## Como testar

**Pré-requisitos:** JDK 21 ou superior e Maven 3.9 ou superior. O repositório não inclui
Maven Wrapper. Os testes são unitários, portanto **não precisam de MySQL** nem de variáveis
de ambiente.

```bash
# Todos os testes
mvn test

# Uma classe específica
mvn test -Dtest=SaldoTest

# Um único método
mvn test -Dtest=GrupoTest#contemNegativoTest
```

Os relatórios ficam em `target/surefire-reports/`.

| Classe de teste | Testes |
|---|---:|
| `rateio.despesa.DespesaTest` | 23 |
| `rateio.despesa.DivisaoServiceTest` | 1 |
| `rateio.grupo.GrupoTest` | 5 |
| `rateio.pagamento.PagamentoTest` | 22 |
| `rateio.quitacao.QuitacaoTest` | 4 |
| `rateio.quitacao.TesteQuitacao` | 4 |
| `rateio.saldo.SaldoTest` | 22 |
| **Total** | **81** |

## Como executar

A aplicação sobe com o Spring Boot e precisa de um MySQL acessível, mas ainda não oferece
endpoints para consumir.

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

3. Gere o JAR e execute:

   ```bash
   mvn -B -DskipTests clean package
   java -jar target/backend-1.0-SNAPSHOT.jar
   ```

Com `spring.jpa.hibernate.ddl-auto=update`, o Hibernate cria ou atualiza as tabelas ao
iniciar.

## Tecnologias e ferramentas

| Categoria | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1.1 (Spring MVC e Spring Data JPA) |
| Banco de dados | MySQL (`mysql-connector-j`) |
| Build | Maven |
| Testes | JUnit 5 e Mockito |
| CI/CD | Jenkins |
| Versionamento | Git e GitHub |
| IDE | IntelliJ IDEA |
| Assistente de IA | Claude Code |

## Uso de IA

A IA foi usada como apoio em testes, CI e documentação.

> **Escopo deste registro:** os itens abaixo vêm do histórico local do Claude Code de
> Gustavo Ferreira Vieira neste repositório, com os prompts transcritos literalmente. O uso
> de IA por outros integrantes não está coberto; cada um deve acrescentar o seu.

| Ferramenta | Modelo | Finalidade |
|---|---|---|
| Claude Code (CLI) | Claude Opus 5.5 (`claude-opus-5-5`) | Testes unitários, stage de deploy, histórias de usuário e este README |

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
`getDataCriacaoMockTest` e `getDataCriacaoAposAlterarMembrosMockTest`, em
`src/test/java/rateio/grupo/GrupoTest.java`. Os outros dois testes da classe
(`adicionarMembroTest` e `contemNegativoTest`) foram escritos pelo autor, que também fez os
commits (`35bd487`, `7caa873` e `87427d4`).

### Stage de deploy no Jenkins (16/10/2026)

**Prompt:**

> add a minimal and simple deploy job for this project, considering the state it is now,
> also following the good practices for Jenkins, as the Jenkinsfile suggests

**Resultado:** a IA adicionou o stage `Deploy` ao `Jenkinsfile`, restrito à branch `main`,
que entrou no histórico pelo commit `dcbc071`.

### Histórias de usuário para o Jira (31/10/2026)

**Prompts:**

1. > create 6 well defined user stories based off of this repository to be added on
   > Atlassian's Jira

2. > turn them into a CSV for Jira's import

**Resultado:** a IA redigiu seis histórias com critérios de aceite a partir do código e gerou
um CSV para importação no Jira.

### Este README (31/10/2026)

**Prompts:**

1. > Update the README file with how the project works, how to test it, technologies/tools
   > used and a dedicated session for AI use (models used, prompts asked, for what, etc)

2. > regarding the README, rewrite it in order to it to hold the project's description,
   > functional and non-functional requirements

**Resultado:** a IA redigiu este documento a partir do código, do `Jenkinsfile`, do `pom.xml`
e do histórico local do Claude Code, e executou `mvn clean test` para confirmar a contagem de
testes (81 testes, nenhuma falha).
