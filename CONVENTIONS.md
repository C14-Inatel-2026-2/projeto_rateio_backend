# Boas Práticas de Versionamento com Git em Projetos de Equipe

Documento de referência para padronizar o uso de Git em repositórios com múltiplos
contribuidores. Cada regra abaixo vem acompanhada da razão pela qual ela existe e,
quando aplicável, da fonte que a sustenta (ver [Fontes](#fontes)).

> **Aviso de escopo:** convenção escrita em README não é controle. Toda regra marcada
> com 🔒 pode e deve ser **imposta por configuração da plataforma ou pela CI**. Regras
> que dependem apenas de disciplina humana serão violadas — é questão de tempo, não de
> caráter. Veja a seção [Automação: transformando regra em barreira](#8-automação-transformando-regra-em-barreira).

---

## Índice

0. [Regra zero](#0-regra-zero)
1. [Branches](#1-branches)
2. [Commits](#2-commits)
3. [Pull Requests](#3-pull-requests-prmerge-requests)
4. [Code Review](#4-code-review)
5. [Merge, rebase e integridade do histórico](#5-merge-rebase-e-integridade-do-histórico)
6. [O que nunca deve ser versionado](#6-o-que-nunca-deve-ser-versionado)
7. [Automação: transformando regra em barreira](#7-automação-transformando-regra-em-barreira)
8. [Tags e releases](#8-tags-e-releases)
9. [Antipadrões comuns](#9-antipadrões-comuns)
10. [Checklists](#10-checklists)
11. [Comandos de referência](#11-comandos-de-referência)
12. [Fontes](#fontes)

---

## 0. Regra zero

**O histórico do Git é documentação de engenharia, não um log de backup.**

Todas as regras deste documento derivam de uma única pergunta prática: *daqui a seis
meses, quando alguém precisar descobrir por que uma linha existe ou reverter uma
mudança específica, o histórico vai ajudar ou atrapalhar?*

Se um commit não pode ser revertido isoladamente, se uma mensagem não explica o
motivo da mudança, ou se ninguém além do autor entendeu o que um PR faz, o
versionamento falhou — mesmo que o código funcione.

---

## 1. Branches

### 1.1 Nunca faça commit direto em `main` (ou `develop`) 🔒

Todo trabalho acontece em uma branch dedicada, criada **antes** da primeira alteração.
Sem exceções para "correção rápida", "só um typo" ou "é só um `println`".

Motivo: commits diretos na branch principal pulam revisão, pulam CI e tornam
impossível reverter uma mudança sem afetar o trabalho de outras pessoas.

```bash
# Errado
git checkout main
# ...edita arquivos...
git commit -am "ajustes"
git push

# Certo
git checkout main
git pull --ff-only origin main
git checkout -b feat/142-calculo-rateio
# ...edita arquivos...
```

### 1.2 Crie a branch a partir de uma base atualizada

Criar branch a partir de uma cópia local desatualizada gera conflitos evitáveis.
Sempre `git pull --ff-only` na branch base antes de ramificar.

### 1.3 Padrão de nomenclatura

Formato: `<tipo>/<id-da-issue>-<descricao-curta-em-kebab-case>`

| Tipo | Uso |
|---|---|
| `feat/` | Nova funcionalidade |
| `fix/` | Correção de defeito |
| `refactor/` | Mudança interna sem alteração de comportamento |
| `test/` | Adição ou correção de testes |
| `docs/` | Documentação |
| `chore/` | Build, dependências, configuração |
| `hotfix/` | Correção urgente em produção |

Exemplos: `feat/142-exportar-relatorio-csv`, `fix/198-divisao-por-zero-no-rateio`.

Evite: `minha-branch`, `teste2`, `joao`, `nova-versao-final`.

### 1.4 Branches devem ser curtas — em tempo, não só em tamanho

A pesquisa DORA associa alto desempenho de entrega a repositórios com **menos de três
branches ativas** e branches com vida útil **inferior a um dia** antes de serem
integradas à branch principal (DORA, *Trunk-based development*).

Isso não é dogma para todo tipo de projeto, mas o mecanismo é objetivo: quanto mais
tempo uma branch vive isolada, maior a divergência acumulada e mais caro o merge.

Se uma tarefa não cabe em uma branch de poucos dias, **o problema é o recorte da
tarefa**, não o Git. Quebre em entregas menores e integráveis.

### 1.5 Sincronize a branch com a base diariamente

```bash
git fetch origin
git rebase origin/main      # se a branch é individual
# ou
git merge origin/main       # se a branch é compartilhada — ver 6.3
```

### 1.6 Apague a branch depois do merge 🔒

Branches mescladas e não removidas poluem o repositório e criam ambiguidade sobre o
que ainda está em andamento. Configure a exclusão automática após o merge.

---

## 2. Commits

### 2.1 Um commit = uma mudança lógica completa (commit atômico)

Um commit gigante contendo funcionalidade nova + refatoração + correção de bug +
formatação é irreversível na prática: não dá para desfazer uma parte sem desfazer as
outras, e `git bisect` deixa de ser útil para localizar a origem de um defeito.

**Critério objetivo:** se a mensagem do commit precisa da palavra "e" para descrever o
que foi feito, provavelmente deveriam ser dois commits.

```
❌ "Adiciona tela de login, corrige bug do rateio, atualiza dependências e formata código"
✅ "Adiciona validação de e-mail no formulário de login"
✅ "Corrige divisão por zero quando o grupo tem um único participante"
✅ "Atualiza Jackson de 2.15.2 para 2.17.0"
```

O caminho inverso também é ruim: 14 commits chamados `wip`, `wip2`, `agora vai`
seguidos de um `fix` são igualmente inúteis. Use *staging* parcial para separar
mudanças que ficaram misturadas na árvore de trabalho:

```bash
git add -p        # seleciona hunks individualmente
git status        # confirme o que está staged antes de commitar
```

Evite `git commit -am` como hábito: ele empacota tudo o que está modificado sem que
você olhe.

### 2.2 Escreva mensagens que expliquem *o quê* e *por quê*

As sete regras consolidadas por Chris Beams (*How to Write a Git Commit Message*) são
o padrão de fato:

1. Separe título e corpo com uma linha em branco.
2. Limite o título a ~50 caracteres (72 é o limite duro).
3. Comece o título com letra maiúscula.
4. Não termine o título com ponto final.
5. Use o modo imperativo no título ("Adiciona", não "Adicionado" / "Adicionando").
6. Quebre o corpo em 72 colunas.
7. Use o corpo para explicar **o quê** e **por quê**, não *como* — o código já mostra o como.

O imperativo não é preferência estética: o próprio Git usa esse modo nas mensagens que
gera automaticamente (por exemplo, em `git merge` e `git revert`).

### 2.3 Adote Conventional Commits

A especificação [Conventional Commits 1.0.0](https://www.conventionalcommits.org/pt-br/v1.0.0/)
define o formato:

```
<tipo>[escopo opcional]: <descrição>

[corpo opcional]

[rodapé opcional]
```

Tipos estruturais definidos pela própria especificação: `feat` (nova funcionalidade,
corresponde a MINOR no SemVer), `fix` (correção, corresponde a PATCH) e
`BREAKING CHANGE:` no corpo ou rodapé (corresponde a MAJOR). Tipos adicionais como
`docs`, `refactor`, `test`, `chore`, `ci`, `perf` e `build` são convenção comum, **não
fazem parte da especificação** e não têm efeito automático sobre a versão.

Exemplo completo:

```
fix(rateio): corrige divisão por zero em grupo sem participantes

O cálculo assumia ao menos um participante ativo. Quando o último era
removido antes do fechamento, a operação lançava ArithmeticException e
derrubava o processamento do lote inteiro.

Passa a retornar rateio vazio e registrar aviso em log.

Closes #198
```

Benefício concreto: mensagens padronizadas são legíveis por máquina, o que permite
geração automática de CHANGELOG e determinação automática da próxima versão.

### 2.4 Não faça commit de código que você não leu

Antes de commitar, revise o próprio diff (`git diff --staged`). A maior parte de
código de depuração esquecido, credencial vazada e arquivo temporário versionado
entra no repositório porque ninguém olhou o que estava sendo enviado.

---

## 3. Pull Requests (PR/Merge Requests)

### 3.1 Nunca aprove o próprio Pull Request 🔒

O objetivo da revisão é obter um segundo par de olhos. Autoaprovação elimina
integralmente esse objetivo e transforma o processo em burocracia decorativa.

Na prática, o GitHub já bloqueia isso: a documentação oficial afirma que autores de
pull request não podem aprovar os próprios PRs. Em outras plataformas isso é
configurável — no Azure DevOps, por exemplo, existe a política de branch
*"Allow requestors to approve their own changes"*, que precisa estar **desmarcada**.

Corolários que costumam ser esquecidos:

- Não faça merge do próprio PR sem a aprovação exigida, mesmo tendo permissão de admin.
- Se você é administrador, ative a opção que aplica as regras de proteção **também aos
  administradores**. Regra que o líder da equipe pula não é regra.
- Não peça "aprova aí" no chat sem que a pessoa tenha lido o diff. Aprovação sem
  leitura é pior do que nenhuma revisão, porque cria falsa confiança.

### 3.2 Mantenha o PR pequeno

O maior estudo publicado sobre revisão de código — conduzido pela SmartBear na Cisco
Systems, com cerca de 2.500 revisões cobrindo 3,2 milhões de linhas ao longo de 10
meses — concluiu que a quantidade de código sob revisão deve ficar **abaixo de 200
linhas e não exceder 400**; acima disso, os revisores são sobrecarregados e param de
encontrar defeitos. O mesmo estudo indica queda acentuada na detecção após cerca de
60–90 minutos contínuos de revisão.

Ou seja: um PR de 3.000 linhas não recebe revisão. Recebe aprovação.

Como reduzir o tamanho:

- Separe refatoração de mudança de comportamento em PRs distintos.
- Envie primeiro a infraestrutura (interfaces, migrações, configuração), depois a lógica.
- Separe formatação/lint automático em um commit ou PR isolado — misturar reformatação
  com lógica torna o diff ilegível.

### 3.3 Descreva o PR para quem não estava no seu lugar

Descrição mínima:

```markdown
## O que muda
Descrição objetiva do comportamento alterado.

## Por que
Problema, issue relacionada ou requisito.

## Como testar
Passos reproduzíveis ou comando de teste.

## Riscos e pontos de atenção
O que pode quebrar, o que ficou fora do escopo, decisões em aberto.
```

"Resolve o bug" não é descrição. Um revisor que precisa reconstruir sozinho o contexto
gasta o orçamento de atenção dele em arqueologia em vez de em análise.

### 3.4 Revise o próprio PR antes de solicitar revisão

Abra o diff no navegador e leia como se fosse de outra pessoa. Comente você mesmo os
trechos não óbvios. Isso remove uma fração significativa dos comentários triviais antes
que alguém precise escrevê-los.

### 3.5 Use Draft/WIP para trabalho incompleto

Um PR aberto para revisão é um pedido de tempo de outra pessoa. Se ainda não está
pronto, marque como rascunho.

### 3.6 Nunca faça merge com a CI vermelha 🔒

Sem exceção "porque o teste é instável". Teste instável é defeito de teste e deve ser
corrigido ou isolado explicitamente, nunca ignorado no merge — um pipeline em que a
falha é rotineiramente ignorada deixa de ter função.

---

## 4. Code Review

### 4.1 Estabeleça e cumpra um prazo de resposta

Combine um SLA (por exemplo, 24 horas úteis). PR parado bloqueia quem o abriu, provoca
conflitos por divergência e incentiva a prática oposta à desejada: acumular mudanças em
lotes maiores. A própria documentação da DORA aponta processos de revisão pesados e
lentos como um dos principais obstáculos ao trabalho em pequenos lotes.

### 4.2 Revise comportamento, não estilo

Formatação, ordenação de imports e convenção de nomes são trabalho de *linter* e
*formatter* na CI, não de ser humano. O revisor humano deve olhar para: correção
lógica, tratamento de erro, casos de borda, concorrência, segurança, legibilidade,
cobertura de teste e aderência ao requisito.

### 4.3 Classifique explicitamente cada comentário

Ambiguidade sobre o que bloqueia e o que é opinião é a principal fonte de atrito em
revisão. Use prefixos:

- **`[bloqueante]`** — precisa ser resolvido antes do merge.
- **`[sugestão]`** — melhoria opcional, fica a critério do autor.
- **`[nit]`** — detalhe menor, não bloqueia.
- **`[dúvida]`** — pedido de esclarecimento, não é crítica.

### 4.4 Critique o código, não a pessoa

"Esse método faz três coisas distintas; sugiro separar" em vez de "você escreveu isso
errado". A diferença não é etiqueta: descrições impessoais e concretas são acionáveis;
julgamentos sobre a pessoa não são.

### 4.5 Aprovar é assumir corresponsabilidade

Quem aprova responde pelo que entrou tanto quanto quem escreveu. Se você não entendeu o
diff, a resposta correta é perguntar — não aprovar.

---

## 5. Merge, rebase e integridade do histórico

### 5.1 Escolha uma estratégia e aplique de forma consistente 🔒

| Estratégia | Resultado | Quando usar |
|---|---|---|
| **Merge commit** | Preserva todos os commits e o ponto de integração | Branches com histórico já limpo e significativo |
| **Squash and merge** | Um commit por PR na branch principal | PRs pequenos; histórico de trabalho ruidoso |
| **Rebase and merge** | Histórico linear sem commit de merge | Equipes que exigem linearidade e commits atômicos bem feitos |

Não há resposta universalmente correta. Há resposta errada: cada pessoa usando uma
estratégia diferente no mesmo repositório. Defina e imponha na configuração do
repositório.

### 6.2 Nunca reescreva histórico já publicado em branch compartilhada 🔒

`git rebase`, `git commit --amend` e `git reset --hard` em commits que já estão em
`main` (ou em qualquer branch usada por outras pessoas) reescrevem hashes e quebram o
repositório local de todos os demais.

- Reescrever histórico **local, ainda não enviado**: adequado e recomendável para
  limpar a branch antes do PR.
- Reescrever histórico **já compartilhado**: proibido. Para desfazer algo já publicado,
  use `git revert`, que cria um novo commit de reversão e preserva o histórico.

Quando precisar atualizar a própria branch após rebase, use
`git push --force-with-lease` em vez de `--force`: a variante `--with-lease` aborta o
envio se alguém tiver publicado algo na branch desde o seu último `fetch`, evitando
sobrescrever trabalho alheio.

### 5.3 Resolva os conflitos na sua branch, não na branch principal

Quem abriu o PR é responsável por integrar a base atualizada e resolver conflitos. Ao
resolver, **entenda as duas versões** — aceitar cegamente "a minha" descarta trabalho de
outra pessoa silenciosamente, e o Git não vai avisar.

### 5.4 `git pull` sem critério gera merges acidentais

Configure o comportamento explicitamente:

```bash
git config --global pull.ff only     # falha em vez de criar merge automático
# quando precisar integrar:
git pull --rebase origin main
```

---

## 6. O que nunca deve ser versionado

### 6.1 Segredos 🔒

Senhas, tokens, chaves de API, certificados privados, credenciais de banco.
**Uma vez commitado, considere o segredo comprometido**, mesmo que removido no commit
seguinte: ele permanece acessível no histórico e em qualquer clone existente.

Procedimento em caso de vazamento, nesta ordem:

1. **Rotacione a credencial imediatamente.** Esta é a única etapa que realmente mitiga.
2. Remova do histórico (`git filter-repo` ou BFG) e comunique a equipe — todos
   precisarão reclonar.
3. Adicione ao `.gitignore` e registre um `.env.example` sem valores reais.

Use varredura automática de segredos na CI ou no lado do servidor.

### 6.2 Artefatos gerados

Binários de build, `target/`, `build/`, `node_modules/`, `*.o`, `*.class`, relatórios de
cobertura, logs. São reproduzíveis a partir do código-fonte, inflam o repositório
permanentemente e conflitam a cada merge.

### 6.3 Configuração pessoal de IDE

`.idea/`, `.vscode/`, `*.iml`, `.DS_Store`. Se um subconjunto for útil ao time
(configuração compartilhada de formatação, por exemplo), versione apenas esses arquivos
específicos e ignore o restante.

### 6.4 Arquivos grandes e binários

Assets binários grandes crescem o `.git` de forma irreversível. Use Git LFS ou um
armazenamento externo com referência versionada.

Mantenha um `.gitignore` no repositório desde o primeiro commit.

---

## 7. Automação: transformando regra em barreira

Esta é a seção que faz as demais funcionarem. **Toda regra marcada com 🔒 acima deve
ser configurada, não confiada.**

### 7.1 Proteção da branch principal

Configuração mínima (nomes variam entre GitHub, GitLab, Bitbucket e Azure DevOps):

- Exigir Pull Request para qualquer alteração — bloquear push direto.
- Exigir **N ≥ 1** aprovação de alguém que não seja o autor.
- Exigir aprovação de *code owner* nos caminhos críticos (`CODEOWNERS`).
- Exigir que os *status checks* da CI passem antes do merge.
- Descartar aprovações antigas quando novos commits forem enviados
  (*dismiss stale approvals*) — caso contrário aprova-se uma versão e integra-se outra.
- Bloquear `force push` e exclusão da branch protegida.
- **Aplicar as regras também aos administradores.**

Detalhe importante do GitHub: aprovações contam apenas quando vêm de pessoas com
permissão de escrita ou administração no repositório; aprovações de quem não tem essas
permissões não liberam o merge.

### 7.2 Verificações automáticas no pipeline

Compilação, testes unitários, testes de integração, cobertura mínima, análise estática e
varredura de segredos como *checks* obrigatórios. Uma regra verificada pela máquina é
cumprida 100% das vezes; uma regra escrita em documento é cumprida enquanto houver
folga no prazo.

### 7.3 Hooks e validação de mensagem

- `commit-msg`: valida o formato Conventional Commits (por exemplo, `commitlint`).
- `pre-commit`: formatação e lint antes do commit.
- Hooks locais são conveniência, não controle — podem ser burlados com `--no-verify`.
  A validação que vale é a que roda no servidor/CI.

### 7.4 Templates

Versione `.github/PULL_REQUEST_TEMPLATE.md` e um template de mensagem de commit
(`git config commit.template`). Reduzir o atrito de fazer certo é mais eficaz do que
cobrar depois.

---

## 8. Tags e releases

- Versione releases com [Semantic Versioning](https://semver.org/lang/pt-BR/):
  `MAJOR.MINOR.PATCH`.
- Use **tags anotadas**, que armazenam autor, data e mensagem:
  `git tag -a v1.2.0 -m "Release 1.2.0"` — tags leves não guardam esses metadados.
- Tags apontam para commits em `main`; nunca mova uma tag já publicada.
- Gere o CHANGELOG a partir dos Conventional Commits em vez de mantê-lo manualmente.

---

## 9. Antipadrões comuns

| Antipadrão | Consequência concreta |
|---|---|
| Commit direto em `main` | Sem revisão, sem CI, reversão arriscada |
| Commit gigante multipropósito | Impossível reverter parcialmente; `git bisect` inútil |
| Série de commits `wip`, `wip2`, `fix` | Histórico não auditável |
| Mensagem "ajustes", "correções", "update" | Zero informação sobre o motivo da mudança |
| Autoaprovação de PR | Revisão vira formalidade |
| PR de milhares de linhas | Aprovado sem leitura real (evidência SmartBear/Cisco) |
| `git push --force` em branch compartilhada | Perda silenciosa de trabalho alheio |
| Merge com CI vermelha | Normaliza pipeline quebrado; a CI perde função |
| Branch viva por semanas | Divergência acumulada e merge caro |
| Segredo commitado | Credencial comprometida de forma permanente no histórico |
| Reformatação misturada com lógica | Diff ilegível; defeito passa despercebido |
| Branch órfã não removida após merge | Ambiguidade sobre o que está em andamento |

---

## 10. Checklists

### Antes de abrir o PR

- [ ] Branch criada a partir da base atualizada e sincronizada com ela
- [ ] Commits atômicos, com mensagens no imperativo e no padrão acordado
- [ ] Nenhum segredo, artefato de build ou arquivo temporário no diff
- [ ] Testes escritos e passando localmente
- [ ] Diff revisado por mim mesmo, do início ao fim
- [ ] Descrição preenchida: o que, por que, como testar, riscos
- [ ] Escopo único; refatoração ampla e formatação separadas
- [ ] Tamanho dentro do razoável para revisão (referência: < 400 linhas alteradas)

### Antes de aprovar um PR de outra pessoa

- [ ] Eu li o diff inteiro — não apenas a descrição
- [ ] Entendi o problema que está sendo resolvido
- [ ] Verifiquei casos de borda e tratamento de erro
- [ ] Confirmei que existem testes cobrindo o comportamento alterado
- [ ] A CI está verde
- [ ] Meus comentários estão classificados (bloqueante / sugestão / nit / dúvida)
- [ ] Estou disposto a assumir corresponsabilidade por este código

---

## 11. Comandos de referência

```bash
# Iniciar trabalho
git checkout main
git pull --ff-only origin main
git checkout -b feat/142-descricao-curta

# Commit seletivo e revisado
git add -p
git diff --staged
git commit                       # abre o editor: título + corpo

# Sincronizar branch individual com a base
git fetch origin
git rebase origin/main
git push --force-with-lease      # nunca --force puro

# Corrigir o último commit ainda NÃO publicado
git commit --amend

# Desfazer algo JÁ publicado
git revert <hash>

# Inspecionar histórico
git log --oneline --graph --decorate --all
git log -p <arquivo>             # evolução de um arquivo
git blame <arquivo>              # origem de cada linha
git bisect start                 # localizar o commit que introduziu um defeito

# Guardar trabalho temporário
git stash push -m "descricao"
git stash pop
```

---

## Fontes

Cada afirmação factual ou numérica deste documento tem origem nas referências abaixo.

- **Conventional Commits 1.0.0** — especificação do formato de mensagem e sua relação
  com SemVer (tipos `feat`/MINOR, `fix`/PATCH, `BREAKING CHANGE`/MAJOR; tipos adicionais
  não são mandatórios pela especificação).
  <https://www.conventionalcommits.org/pt-br/v1.0.0/>
- **Chris Beams, *How to Write a Git Commit Message*** — as sete regras (linha em branco
  entre título e corpo, 50/72 caracteres, modo imperativo, "o quê e por quê" no corpo).
  <https://cbea.ms/git-commit/>
- **Documentação do GitHub, *Approving a pull request with required reviews*** — autores
  de pull request não podem aprovar os próprios PRs; aprovações só contam quando vêm de
  usuários com permissão de escrita ou administração.
  <https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/reviewing-changes-in-pull-requests/approving-a-pull-request-with-required-reviews>
- **SmartBear / Cisco Systems, *Best Kept Secrets of Peer Code Review*** — estudo de
  ~2.500 revisões sobre 3,2 milhões de linhas em 10 meses; conclusão de que o volume sob
  revisão deve ficar abaixo de 200 linhas e não exceder 400, com queda de detecção após
  cerca de 60–90 minutos de revisão contínua.
  <https://static0.smartbear.co/support/media/resources/cc/book/code-review-cisco-case-study.pdf>
- **DORA, *Trunk-based development* e *Continuous delivery*** — menos de três branches
  ativas, vida útil de branch inferior a um dia, e processos de revisão pesados como
  obstáculo ao trabalho em pequenos lotes.
  <https://dora.dev/capabilities/trunk-based-development/>
- **Vincent Driessen, *A successful Git branching model*** — inclui a nota de reflexão de
  5 de março de 2020, em que o próprio autor recomenda um fluxo mais simples (como o
  GitHub Flow) para equipes que praticam entrega contínua, em vez de forçar o git-flow.
  <https://nvie.com/posts/a-successful-git-branching-model/>
- **Semantic Versioning 2.0.0** — esquema `MAJOR.MINOR.PATCH`.
  <https://semver.org/lang/pt-BR/>
- **Documentação oficial do Git** — comportamento de `revert`, `rebase`,
  `push --force-with-lease`, tags anotadas e `bisect`.
  <https://git-scm.com/docs>

**Observações de transparência:** os limites numéricos citados (200–400 linhas por
revisão; branches com menos de 24 horas) são *achados empíricos correlacionais* dos
estudos citados, não leis universais — o estudo da Cisco foi conduzido entre 2005 e 2006
em um contexto específico, e os dados da DORA descrevem correlação com desempenho de
entrega, não causalidade demonstrada em todo tipo de projeto. Trate-os como referência
calibrada, não como limiar absoluto. As demais recomendações deste documento
(nomenclatura de branches, classificação de comentários de revisão, estrutura de
descrição de PR) são **convenções de equipe** sem base experimental publicada: sua
justificativa é a consistência interna, e podem ser adaptadas desde que aplicadas de
forma uniforme.
