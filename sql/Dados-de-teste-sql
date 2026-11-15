-- Dados de teste para o catálogo. Execute DEPOIS dos dois scripts principais,
-- em um banco recém-criado (os ids abaixo assumem tabelas vazias).
USE biblioteca_digital;

INSERT INTO editora (nome, pais) VALUES ('Editora Exemplo A', 'Brasil'), ('Editora Exemplo B', 'Brasil');

INSERT INTO autor (nome, nacionalidade) VALUES
  ('Machado de Assis', 'Brasileira'),
  ('Clarice Lispector', 'Brasileira'),
  ('George Orwell', 'Britânica');

INSERT INTO genero (nome) VALUES ('Romance'), ('Ficção'), ('Clássico');

-- Senhas de teste (BCrypt): admin@biblioteca.com = admin123 | leitor@biblioteca.com = leitor123
INSERT INTO usuario (nome, email, senha_hash, perfil) VALUES
  ('Administrador', 'admin@biblioteca.com', '$2b$10$2KkP9a3ddYHrJ1DKEuK6H.Mat3BIVxK7N/Xo1g.p0y.Opv9FL9sQ2', 'administrador'),
  ('Leitor Teste', 'leitor@biblioteca.com', '$2b$10$VVSKm9zoU.SiC6E9nGa21eopP2ghrwqvz0ZhYa5ZurMhitk1MRrk6', 'usuario');

INSERT INTO obra (titulo, sinopse, idioma, ano_publicacao, id_editora, id_admin_cadastro) VALUES
  ('Dom Casmurro', 'Bentinho relembra a juventude e duvida da fidelidade de Capitu.', 'Português', 1899, 1, 1),
  ('A Hora da Estrela', 'A história de Macabéa, uma jovem nordestina que vive no Rio de Janeiro.', 'Português', 1977, 2, 1),
  ('1984', 'Em um Estado totalitário, Winston tenta escapar da vigilância constante.', 'Português', 1949, 1, 1);

INSERT INTO obra_autor (id_obra, id_autor) VALUES (1, 1), (2, 2), (3, 3);
INSERT INTO obra_genero (id_obra, id_genero) VALUES (1, 1), (1, 3), (2, 1), (3, 2);

INSERT INTO ebook (id_obra, formato, tamanho_mb, numero_paginas, arquivo_url, valor_emprestimo) VALUES
  (1, 'EPUB', 1.20, 256, '/arquivos/dom-casmurro.epub', 9.90),
  (3, 'PDF', 2.40, 328, '/arquivos/1984.pdf', 12.90);

INSERT INTO audiobook (id_obra, narrador, duracao_minutos, formato, tamanho_mb, arquivo_url, valor_emprestimo) VALUES
  (3, 'Narrador Exemplo', 720, 'MP3', 350.50, '/arquivos/1984.mp3', 14.90);

INSERT INTO avaliacao (id_usuario, id_obra, nota, comentario) VALUES
  (2, 1, 5, 'Excelente'),
  (2, 3, 4, 'Muito bom');
