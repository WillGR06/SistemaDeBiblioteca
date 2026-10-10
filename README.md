# Sistema de Gerenciamento de Biblioteca

## 1. Sobre o projeto

O diferencial do Sistema de Gerenciamento de Biblioteca é a organização e automatização das principais funções de uma biblioteca, como o gerenciamento do acervo, cadastro de usuários, empréstimos, devoluções e controle de atrasos.

O projeto está sendo desenvolvido em grupo, com foco em Engenharia de Software, levantamento de requisitos, modelagem de sistemas e testes.

## 2. Objetivos

- Organizar o cadastro e a consulta de livros.
- Facilitar o controle de empréstimos e devoluções.
- Acompanhar a disponibilidade dos exemplares.
- Identificar empréstimos atrasados e eventuais multas.
- Facilitar o gerenciamento dos usuários e do acervo.
- Oferecer informações para auxiliar a administração da biblioteca.

## 3. Escopo do sistema

### Funcionalidades previstas

- Cadastro e gerenciamento de livros e exemplares.
- Cadastro e gerenciamento de usuários.
- Consulta ao catálogo da biblioteca.
- Registro de empréstimos e devoluções.
- Controle de prazos, atrasos e multas.
- Reservas de livros.
- Envio de notificações e lembretes.
- Consulta ao histórico de empréstimos.
- Relatórios administrativos.

### Fora do escopo inicial

- Compra e venda de livros.
- Integração com sistemas externos de bibliotecas.
- Aplicativo móvel nativo.
- Pagamentos on-line de multas.

Esses itens poderão ser reconsiderados futuramente, conforme as decisões do grupo.

---

## 4. Requisitos funcionais

Os requisitos funcionais descrevem as funcionalidades e os comportamentos que o sistema deverá oferecer.

| ID | Nome | Descrição | Categoria |
|---|---|---|---|
| RF 1.1 | Cadastro de livros | O sistema deve permitir que usuários autorizados cadastrem livros, informando título, autor, ISBN, editora, ano de publicação e gênero. | Gerenciamento do acervo |
| RF 1.2 | Edição de livros | O sistema deve permitir a atualização dos dados de livros cadastrados. | Gerenciamento do acervo |
| RF 1.3 | Remoção de livros | O sistema deve permitir a retirada de livros do catálogo, respeitando as regras para exemplares emprestados ou reservados. | Gerenciamento do acervo |
| RF 1.4 | Cadastro de exemplares | O sistema deve permitir o registro de exemplares físicos de um mesmo livro, com identificação individual. | Gerenciamento do acervo |
| RF 1.5 | Consulta ao catálogo | O sistema deve permitir a busca de livros por título, autor, ISBN ou gênero. | Consulta |
| RF 1.6 | Consulta de disponibilidade | O sistema deve informar se existem exemplares disponíveis para empréstimo. | Consulta |
| RF 2.1 | Cadastro de usuários | O sistema deve permitir o cadastro de usuários da biblioteca, armazenando os dados necessários para sua identificação. | Gerenciamento de usuários |
| RF 2.2 | Atualização de usuários | O sistema deve permitir a atualização dos dados dos usuários cadastrados. | Gerenciamento de usuários |
| RF 2.3 | Bloqueio de usuários | O sistema deve permitir o bloqueio de usuários que não atendam às regras de utilização da biblioteca. | Gerenciamento de usuários |
| RF 3.1 | Registro de empréstimo | O sistema deve permitir que um bibliotecário registre o empréstimo de um exemplar disponível para um usuário habilitado. | Empréstimos |
| RF 3.2 | Definição de prazo | O sistema deve calcular ou registrar a data prevista para a devolução de cada empréstimo. | Empréstimos |
| RF 3.3 | Validação de empréstimo | O sistema deve verificar a disponibilidade do exemplar e a elegibilidade do usuário antes de confirmar um empréstimo. | Empréstimos |
| RF 3.4 | Renovação de empréstimo | O sistema deve permitir a renovação de um empréstimo quando as regras da biblioteca forem atendidas. | Empréstimos |
| RF 4.1 | Registro de devolução | O sistema deve permitir o registro da devolução de um exemplar emprestado. | Devoluções |
| RF 4.2 | Verificação de atraso | O sistema deve verificar se a devolução ocorreu após a data prevista. | Devoluções |
| RF 4.3 | Cálculo de multa | O sistema deve calcular a multa por atraso conforme as regras definidas pela biblioteca. | Devoluções |
| RF 4.4 | Atualização de disponibilidade | Após a devolução, o sistema deve atualizar a situação do exemplar, considerando eventuais reservas pendentes. | Devoluções |
| RF 5.1 | Reserva de livros | O sistema deve permitir que usuários habilitados solicitem a reserva de um livro indisponível. | Reservas |
| RF 5.2 | Controle de fila de reservas | O sistema deve organizar as reservas conforme uma regra de prioridade definida pela biblioteca. | Reservas |
| RF 5.3 | Cancelamento de reserva | O sistema deve permitir o cancelamento de uma reserva conforme as regras estabelecidas. | Reservas |
| RF 6.1 | Avisos de vencimento | O sistema deve enviar ou disponibilizar lembretes aos usuários antes da data prevista para a devolução. | Notificações |
| RF 6.2 | Avisos de atraso | O sistema deve notificar os usuários sobre empréstimos em atraso. | Notificações |
| RF 6.3 | Avisos de reserva | O sistema deve informar o usuário quando um exemplar reservado estiver disponível para retirada. | Notificações |
| RF 7.1 | Histórico de empréstimos | O sistema deve manter o histórico de empréstimos, renovações e devoluções dos usuários. | Histórico |
| RF 7.2 | Histórico do exemplar | O sistema deve permitir a consulta ao histórico de empréstimos de cada exemplar. | Histórico |
| RF 8.1 | Relatório de empréstimos | O sistema deve gerar relatórios de empréstimos realizados em um período selecionado. | Relatórios |
| RF 8.2 | Relatório de atrasos | O sistema deve apresentar os empréstimos em atraso e as respectivas multas, quando aplicáveis. | Relatórios |
| RF 8.3 | Relatório do acervo | O sistema deve apresentar informações sobre a quantidade de livros, exemplares disponíveis, emprestados e indisponíveis. | Relatórios |

## 5. Requisitos não funcionais

Os requisitos não funcionais definem critérios de qualidade, segurança e operação do sistema.

| ID | Nome | Descrição | Categoria |
|---|---|---|---|
| RNF 1.1 | Segurança de acesso | O sistema deve restringir funcionalidades conforme o perfil de acesso do usuário. | Segurança |
| RNF 1.2 | Proteção de dados | O sistema deve proteger os dados pessoais dos usuários contra acesso não autorizado. | Segurança e privacidade |
| RNF 1.3 | Autenticação | As funcionalidades administrativas devem exigir autenticação. | Segurança |
| RNF 2.1 | Integridade dos dados | O sistema deve impedir que um mesmo exemplar seja emprestado simultaneamente para mais de um usuário. | Confiabilidade |
| RNF 2.2 | Consistência | As operações de empréstimo e devolução devem manter atualizadas as informações de disponibilidade dos exemplares. | Confiabilidade |
| RNF 3.1 | Desempenho | As consultas comuns ao catálogo devem apresentar resultados em tempo adequado para uso cotidiano. | Desempenho |
| RNF 3.2 | Usabilidade | A interface deve apresentar informações e mensagens de erro de forma clara e compreensível. | Usabilidade |
| RNF 3.3 | Acessibilidade | A interface deve buscar atender às boas práticas de acessibilidade digital. | Acessibilidade |
| RNF 4.1 | Manutenibilidade | O sistema deve ser organizado de forma que suas funcionalidades possam ser corrigidas e ampliadas com facilidade. | Manutenibilidade |
| RNF 4.2 | Testabilidade | As funcionalidades críticas devem permitir a realização de testes automatizados ou manuais reproduzíveis. | Testabilidade |
| RNF 5.1 | Disponibilidade | O sistema deve estar disponível durante os períodos de funcionamento definidos para a biblioteca, respeitando as limitações da infraestrutura escolhida. | Disponibilidade |
| RNF 5.2 | Backup e recuperação | O sistema deve possuir uma estratégia de cópia de segurança e recuperação dos dados. | Recuperação |
| RNF 6.1 | Portabilidade | A solução deve possuir documentação suficiente para permitir sua instalação e execução em ambientes compatíveis. | Portabilidade |


## 6. Regras de negócio

As regras de negócio definem as condições da biblioteca que devem ser seguidas.

| ID | Nome | Descrição |
|---|---|---|
| RN 1 | Prazo de empréstimo | Cada empréstimo deve possuir uma data prevista para devolução, calculada conforme a política da biblioteca. |
| RN 2 | Disponibilidade | Somente exemplares disponíveis podem ser emprestados. |
| RN 3 | Limite de empréstimos | Um usuário não pode ultrapassar o limite de empréstimos simultâneos definido pela biblioteca. |
| RN 4 | Elegibilidade do usuário | Usuários bloqueados ou impedidos pelas regras da biblioteca não podem realizar novos empréstimos. |
| RN 5 | Devolução | Um empréstimo deve ser encerrado quando a devolução do exemplar for registrada. |
| RN 6 | Multa por atraso | A multa deve ser calculada conforme o período de atraso e a política vigente da biblioteca. |
| RN 7 | Renovação | A renovação não deve ser permitida quando houver restrições definidas pela biblioteca, como uma reserva de outro usuário. |
| RN 8 | Reserva | Um exemplar reservado deve respeitar a ordem de atendimento e o prazo de retirada estabelecidos pela biblioteca. |
| RN 9 | Integridade do acervo | Um exemplar não pode possuir mais de um empréstimo ativo simultaneamente. |
| RN 10 | Exclusão de registros | Registros que façam parte do histórico de empréstimos não devem ser removidos de forma que comprometa a rastreabilidade das operações. |

## 7. Perfis de usuário

| Perfil | Responsabilidades |
|---|---|
| Administrador | Gerenciar configurações, permissões e cadastros administrativos. |
| Bibliotecário | Gerenciar o acervo, registrar empréstimos e devoluções e consultar relatórios. |
| Leitor | Consultar o catálogo, acompanhar seus empréstimos e realizar reservas, conforme as permissões definidas. |

## 8. Tecnologias

As tecnologias estão sendo definidas.

| Componente | Tecnologia escolhida |
|---|---|
| Linguagem de programação | A definir |
| Interface | A definir |
| Banco de dados | A definir |
| Testes | A definir |
| Controle de versão | Git e GitHub |

## 9. Etapas previstas

1. Levantamento e validação dos requisitos.
2. Definição do escopo inicial.
3. Modelagem do sistema e dos dados.
4. Escolha das tecnologias.
5. Planejamento das tarefas e divisão das responsabilidades.
6. Implementação das funcionalidades prioritárias.
7. Testes e correções.
8. Documentação e entrega.

## 10. Contribuição

1. Discuta as alterações que quer fazer
2. Registre novas funcionalidades ou mudanças nos requisitos.
3. Utilize branches para desenvolver funcionalidades separadamente.
4. Abra pull requests para revisão das alterações.
5. Atualize a documentação quando houver mudanças no comportamento do sistema.

## 11. Status do projeto

**Fase atual:** levantamento e documentação de requisitos.

O escopo, as regras de negócio e as tecnologias serão discutidos.
