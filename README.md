# Biblioteca Digital (Spring Boot + MySQL)

## Como rodar no IntelliJ
1. **File → Open** e selecione a pasta `biblioteca-digital` (ou o `pom.xml`). Aguarde o Maven baixar as dependências.
2. **File → Project Structure → Project SDK**: escolha o **JDK 17 ou superior**.
3. No MySQL Workbench, execute nesta ordem os arquivos da pasta `sql/`:
   `01_biblioteca_digital.sql`, `02_biblioteca_logica.sql` e `03_dados_exemplo.sql`.
4. Abra `src/main/resources/application.properties` e troque `SUA_SENHA_AQUI` pela senha do seu MySQL.
5. Abra `BibliotecaApplication.java` e clique no ▶ ao lado do `main`.
6. Acesse http://localhost:8080

## Contas de teste (criadas por `03_dados_exemplo.sql`)
| Perfil        | E-mail                  | Senha      |
|---------------|-------------------------|------------|
| Administrador | admin@biblioteca.com    | admin123   |
| Leitor        | leitor@biblioteca.com   | leitor123  |

## Telas
- `/` e `/livros` — catálogo com busca por título ou autor
- `/livros/{id}` — detalhes da obra, empréstimo de e-book/audiobook (30 dias) e avaliações (nota de 1 a 5)
- `/cadastro` e `/login` — criar conta e entrar
- `/emprestimos` — meus empréstimos (situação, dias restantes, devolver, ler)
- `/emprestimos/{id}/ler` — leitura liberada só enquanto o empréstimo está ativo
- `/admin/obras` — (somente administrador) cadastrar, editar e excluir obras
- `/admin/membros` — (somente administrador) listar, buscar, ativar/desativar e mudar o perfil dos membros
- `/admin/log` — (somente administrador) histórico de ações gravado pelas triggers

## Estrutura
- `model/` — objetos de dados (records)
- `repository/` — acesso ao banco com `JdbcTemplate` (chama as procedures e functions)
- `controller/` — recebe as requisições e escolhe a tela
- `security/` e `config/` — login com Spring Security (BCrypt) e permissões por perfil
- `templates/` — páginas HTML (Thymeleaf)
- `sql/` — scripts do banco

## Testes

**Testes unitários** (não precisam de banco, usam Mockito): ficam em `src/test/java/br/unisul/biblioteca/unit/` e nos arquivos `LivroTest` e `ErrosTest`.

| Classe | O que testar |
|---|---|
| `AuthControllerUnitTest` | validações do cadastro (nome, e-mail, senha) e criptografia da senha |
| `LivroControllerUnitTest` | catálogo e detalhe do livro (404, visitante e usuário logado) |
| `EmprestimoControllerUnitTest` | emprestar, devolver, ler e salvar progresso, com sucesso e com erro |
| `AdminControllerUnitTest` | cadastro, edição e exclusão de obras, validação do título |
| `MembroControllerUnitTest` | ativar, desativar e mudar o perfil de membros |
| `AvaliacaoControllerUnitTest` | avaliar livro, livro inexistente e nota inválida |
| `RepositoriosUnitTest` | regras de validação dos repositórios (nota, comentário, perfil, progresso) |
| `SegurancaUnitTest` | papéis do login, membro inativo, mensagens de erro e formulário padrão |

**Testes de integração** (usam o MySQL de verdade, com um banco **separado**, `biblioteca_digital_teste`,
para nunca mexer nos seus dados):
1. No Workbench, execute `sql/teste/01_tabelas_teste.sql` e depois `sql/teste/02_logica_teste.sql`.
   (Não precisa rodar o `03_dados_exemplo.sql` no banco de teste: cada teste cria seus próprios dados.)
2. O usuário e a senha do MySQL são os mesmos do `application.properties`.
3. Rode todos com `mvn test` ou clique com o botão direito em `src/test` → **Run Tests**.

| Classe | O que testar |
|---|---|
| `EmprestimoIntegracaoTest` | procedure de empréstimo e devolução, prazo de 30 dias, bloqueio, function de dias restantes, progresso |
| `AdminIntegracaoTest` | cadastro, edição e exclusão de obras, triggers de log e de valor negativo, rollback |
| `LivroIntegracaoTest` | busca por título e autor, média de avaliações, formatos, regra da nota (1 a 5) |
| `UsuarioIntegracaoTest` | cadastro, perfil padrão, e-mail duplicado |
| `AvaliacaoIntegracaoTest` | avaliar, atualizar sem duplicar, nota fora de 1 a 5, média |
| `MembroIntegracaoTest` | listar e buscar membros, ativar/desativar, mudar perfil, histórico, proteções do próprio admin |
| `MembroAvaliacaoWebIntegracaoTest` | telas de avaliação e de membros, permissões, CSRF e bloqueio de login de membro desativado |
| `WebIntegracaoTest` | telas públicas, login, permissões (leitor x administrador), CSRF e o fluxo completo emprestar → ler → devolver |

## Qualidade e automação
- **Pipeline (GitHub Actions):** a cada push ou pull request na `main`, o arquivo `.github/workflows/ci.yml` sobe um MySQL, cria o banco de teste, roda todos os testes e mede a cobertura com o **JaCoCo**. O build falha se a cobertura de linhas for menor que **75%**.
- **Análise estática (SonarCloud):** se o segredo `SONAR_TOKEN` estiver configurado no repositório, a pipeline envia a análise e espera o **quality gate**.
  1. Crie o projeto em https://sonarcloud.io (importando o repositório do GitHub).
  2. Em `pom.xml`, troque `SUA_ORGANIZACAO` e a `sonar.projectKey` pelos dados do projeto.
  3. No GitHub: *Settings → Secrets and variables → Actions → New repository secret* com o nome `SONAR_TOKEN`.
- **Rodar localmente:** `mvn verify` (precisa do banco `biblioteca_digital_teste`, veja a seção Testes). O relatório de cobertura fica em `target/site/jacoco/index.html`.
- Regras de branches e commits: veja `CONTRIBUTING.md`.
