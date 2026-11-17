# Como contribuir

## Branches
- A branch principal é a **`main`**. Ela sempre deve compilar e passar nos testes.
- Para cada tarefa, crie uma branch a partir da `main` (ex.: `feat/avaliacao-de-livros`) e abra um *pull request*.
- O pull request só é aceito se a pipeline (GitHub Actions) passar: testes, cobertura mínima de 75% e quality gate do SonarCloud.

## Mensagens de commit (Conventional Commits)
Formato: `tipo(escopo): descrição curta no imperativo`

| Tipo | Quando usar | Exemplo |
|---|---|---|
| `feat` | nova funcionalidade | `feat(emprestimo): bloquear acesso após 30 dias` |
| `fix` | correção de erro | `fix(login): impedir entrada de membro desativado` |
| `test` | testes | `test(membro): cobrir desativação do próprio administrador` |
| `refactor` | melhoria sem mudar o comportamento | `refactor(admin): dividir o método salvar` |
| `docs` | documentação | `docs: atualizar o README` |
| `ci` | pipeline e configuração | `ci: adicionar análise do SonarCloud` |
| `chore` | tarefas de manutenção | `chore: atualizar dependências` |

## Estrutura do repositório
```
.
├── .github/workflows/ci.yml   pipeline (testes, cobertura e SonarCloud)
├── LICENSE                    licença MIT
├── README.md                  como rodar o projeto
├── CONTRIBUTING.md            regras de contribuição
├── pom.xml                    dependências, JaCoCo e SonarCloud
├── sql/                       scripts do banco (e sql/teste/ para os testes)
└── src/
    ├── main/                  código-fonte e telas
    └── test/                  testes unitários e de integração
```
