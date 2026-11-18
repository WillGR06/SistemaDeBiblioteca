# Biblioteca Digital

Sistema web de biblioteca digital desenvolvido como trabalho da UNISUL. O leitor "empresta" e-books e audiobooks por 30 dias e o administrador cadastra as obras e gerencia os membros. O projeto inclui banco de dados MySQL, site em Java, plano de testes, automação com GitHub Actions e análise estática com SonarCloud.

## Regras do sistema
- O empréstimo é a "compra" de um **e-book ou audiobook por 30 dias**. Não há multa: depois do prazo o acesso é bloqueado, o que equivale à devolução.
- Existem dois perfis: **leitor** e **administrador**. Só o administrador cadastra e edita obras e membros.
- Não existem livros físicos, assinatura nem compra avulsa.

## Tecnologias
- Java 17 ou superior (testado com JDK 17 e JDK 24)
- Spring Boot 3.3.5, Spring Security (BCrypt e proteção CSRF) e Thymeleaf
- JDBC (`JdbcTemplate`) e MySQL 8
- JUnit 5 e Mockito para os testes; JaCoCo para a cobertura
- GitHub Actions e SonarCloud

## Funcionalidades
**Leitor**
- Catálogo com busca por título ou autor, com a capa de cada livro
- Detalhe da obra, avaliações (nota de 1 a 5) e empréstimo de e-book ou audiobook
- Cadastro, login, "Meus empréstimos" (situação, dias restantes e devolução)
- Leitura liberada só enquanto o empréstimo está ativo, com **progresso por página**: o leitor informa de 0 até o total de páginas do e-book. Audiobooks e e-books sem número de páginas usam o progresso em porcentagem.

**Administrador**
- Cadastro, edição e exclusão de obras, com botões para escolher se a obra tem **e-book**, **audiobook** ou os dois
- **Cadastro de editoras** direto no formulário da obra ("+ Nova editora")
- **Envio da imagem de capa** (JPG, PNG, GIF ou WEBP, até 5 MB)
- Gestão de membros: buscar, ativar, desativar e mudar o perfil
- Histórico das ações do administrador, gravado por triggers do banco

## Como rodar no IntelliJ
1. **File → Open** e selecione a pasta do projeto (a que tem o `pom.xml`). Aguarde o Maven baixar as dependências.
2. Em **File → Project Structure → Project SDK**, escolha o JDK 17 ou superior.
3. No MySQL Workbench, execute nesta ordem os arquivos da pasta `sql/`:
   `01_biblioteca_digital.sql`, `02_biblioteca_logica.sql` e `03_dados_exemplo.sql`.
4. **Informe a senha do seu MySQL.** Ela não fica no `application.properties`, porque esse arquivo vai para o GitHub. Na raiz do projeto, ao lado do `pom.xml`, crie o arquivo `application-local.properties` com uma linha:
   ```
   spring.datasource.password=SUA_SENHA_DO_MYSQL
   ```
   Esse arquivo está no `.gitignore` e nunca é enviado. Se o seu usuário do MySQL não for `root`, adicione também `spring.datasource.username=SEU_USUARIO`.
5. Abra `BibliotecaApplication.java` e clique no ▶ ao lado do `main`.
6. Acesse http://localhost:8080

As capas enviadas pelo site ficam na pasta `uploads/capas/`, criada automaticamente.

## Contas de exemplo (criadas por `03_dados_exemplo.sql`)
| Perfil        | E-mail                  | Senha      |
|---------------|-------------------------|------------|
| Administrador | admin@biblioteca.com    | admin123   |
| Leitor        | leitor@biblioteca.com   | leitor123  |

Os livros podem ser cadastrados pelo próprio site, pela conta do administrador.

## Telas
- `/` e `/livros`: catálogo com busca
- `/livros/{id}`: detalhes da obra, empréstimo e avaliações
- `/cadastro` e `/login`: criar conta e entrar
- `/emprestimos`: meus empréstimos
- `/emprestimos/{id}/ler`: leitura e progresso
- `/admin/obras`: (administrador) cadastrar, editar e excluir obras
- `/admin/membros`: (administrador) gerenciar membros
- `/admin/log`: (administrador) histórico de ações

## Estrutura do código
- `model/`: objetos de dados (records)
- `repository/`: acesso ao banco com `JdbcTemplate` (chama as procedures e functions)
- `controller/`: recebe as requisições e escolhe a tela
- `security/` e `config/`: login com Spring Security e permissões por perfil
- `util/`: mensagens de erro e armazenamento das capas
- `templates/`: páginas HTML (Thymeleaf)
- `sql/`: scripts do banco (14 tabelas, view, evento de bloqueio, 2 functions, 2 procedures e 15 triggers)

## Testes

**Testes unitários** (não precisam de banco; usam Mockito). Ficam em `src/test/java/br/unisul/biblioteca/unit/` e nos arquivos `LivroTest`, `ErrosTest` e `ModelosErrosRamosTest`.

| Classe | O que testa |
|---|---|
| `AuthControllerUnitTest` | validações do cadastro e criptografia da senha |
| `SegurancaUnitTest` | papéis do login, membro inativo e mensagens de erro |
| `LivroControllerUnitTest` | catálogo e detalhe do livro (404, visitante e usuário logado) |
| `EmprestimoControllerUnitTest` | emprestar, devolver, ler e salvar progresso (em % e por página) |
| `AdminControllerUnitTest` | cadastro, edição e exclusão de obras, cadastro de editora, envio de capa |
| `AdminRepositoryUnitTest` | caminhos do repositório de obras (obra nova ou existente, autor, e-book e audiobook) |
| `ArmazenamentoCapasUnitTest` e `ArmazenamentoCapasRamosUnitTest` | validação das imagens (tipo, tamanho), gravação e exclusão de capas |
| `MembroControllerUnitTest` | ativar, desativar e mudar o perfil de membros |
| `AvaliacaoControllerUnitTest` | avaliar livro, livro inexistente e nota inválida |
| `RepositoriosUnitTest` e `RepositoriosConsultasUnitTest` | regras de validação e conversão das linhas do banco nos repositórios |

**Testes de integração** (usam o MySQL de verdade, com um banco **separado**, `biblioteca_digital_teste`, para nunca mexer nos seus dados):
1. No Workbench, execute `sql/teste/01_tabelas_teste.sql` e depois `sql/teste/02_logica_teste.sql`. Não é preciso rodar o `03_dados_exemplo.sql` no banco de teste: cada teste cria os próprios dados.
2. O usuário e a senha do MySQL são os mesmos da execução normal (inclusive o `application-local.properties`).
3. Rode com `mvn test` ou botão direito em `src/test` → **Run Tests**.

| Classe | O que testa |
|---|---|
| `EmprestimoIntegracaoTest` | empréstimo e devolução, prazo de 30 dias, bloqueio, dias restantes e progresso (em % e por página) |
| `AdminIntegracaoTest` | cadastro, edição e exclusão de obras, editoras, capa, triggers de log e rollback |
| `LivroIntegracaoTest` | busca por título e autor, média de avaliações, formatos e regra da nota |
| `UsuarioIntegracaoTest` | cadastro, perfil padrão e e-mail duplicado |
| `AvaliacaoIntegracaoTest` | avaliar, atualizar sem duplicar, nota fora de 1 a 5 e média |
| `MembroIntegracaoTest` | listar e buscar membros, ativar e desativar, mudar perfil e proteções do admin |
| `MembroAvaliacaoWebIntegracaoTest` | telas de avaliação e membros, permissões, CSRF e bloqueio de membro desativado |
| `WebIntegracaoTest` | telas públicas, login, permissões, CSRF e o fluxo emprestar → ler → devolver |

## Qualidade e automação
- **Pipeline (GitHub Actions):** a cada push ou pull request na `main`, o `.github/workflows/ci.yml` sobe um MySQL, cria o banco de teste, roda todos os testes e mede a cobertura com o **JaCoCo**. O build falha se a cobertura de linhas for menor que **75%**. A cobertura medida pelo JaCoCo no projeto completo fica acima de 95%.
- **Análise estática (SonarCloud):** se o segredo `SONAR_TOKEN` existir no repositório, a pipeline envia a análise e espera o **quality gate**. Para configurar:
  1. Crie o projeto em https://sonarcloud.io, importando o repositório do GitHub.
  2. Em **Administration → Analysis Method**, desligue a **Automatic Analysis**. Com ela ligada, a análise da pipeline é recusada.
  3. Confira se `sonar.organization` e `sonar.projectKey`, no `pom.xml`, são exatamente iguais aos do painel do SonarCloud (**Administration → Information**).
  4. No GitHub: *Settings → Secrets and variables → Actions → New repository secret*, com o nome `SONAR_TOKEN` e o token gerado em *My Account → Security* do SonarCloud.
- **Rodar localmente:** `mvn verify` (precisa do banco `biblioteca_digital_teste`). O relatório de cobertura fica em `target/site/jacoco/index.html`.
- Regras de branches e commits: veja o `CONTRIBUTING.md`.

## Equipe
| Aluno | Cargo | Parte do sistema |
|---|---|---|
| William | Desenvolvedor | Base do projeto, login e cadastro de usuários |
| Amanda | Desenvolvedora | Administração de obras: cadastro, editoras, capas e histórico |
| Luiz | Desenvolvedor | Empréstimo, devolução e leitura com progresso por página; pipeline (GitHub Actions e SonarCloud) |
| Guilherme | Desenvolvedor | Catálogo e detalhe do livro; testes dos repositórios |
| Vitor | Desenvolvedor | Gestão de membros e avaliações; testes das telas |

## Licença
Distribuído sob a licença MIT. Veja o arquivo `LICENSE`.
