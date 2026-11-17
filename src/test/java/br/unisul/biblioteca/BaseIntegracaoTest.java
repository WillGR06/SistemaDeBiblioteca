package br.unisul.biblioteca;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

/**
 * Base dos testes de integração: usa o banco biblioteca_digital_teste,
 * limpa tudo antes de cada teste e cria um cenário padrão.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
abstract class BaseIntegracaoTest {

    static final String SENHA_PADRAO = "senha123";

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    protected PasswordEncoder encoder;

    protected int idLeitor;
    protected int idOutroLeitor;
    protected int idAdmin;
    protected int idObra;
    protected int idEbook;
    protected int idAudiobook;

    @BeforeEach
    void prepararBanco() {
        String[] limpezas = {
                "DELETE FROM log_administracao", "DELETE FROM acesso_digital", "DELETE FROM pagamento",
                "DELETE FROM emprestimo", "DELETE FROM avaliacao", "DELETE FROM obra_genero",
                "DELETE FROM obra_autor", "DELETE FROM ebook", "DELETE FROM audiobook", "DELETE FROM obra",
                "DELETE FROM autor", "DELETE FROM genero", "DELETE FROM editora", "DELETE FROM usuario"};
        for (String sql : limpezas) {
            jdbc.update(sql);
        }

        idLeitor = inserirUsuario("Leitor Teste", "leitor@teste.com", "usuario");
        idOutroLeitor = inserirUsuario("Outro Leitor", "outro@teste.com", "usuario");
        idAdmin = inserirUsuario("Admin Teste", "admin@teste.com", "administrador");

        jdbc.update("INSERT INTO autor (nome) VALUES ('Machado de Assis')");
        int idAutor = jdbc.queryForObject("SELECT id_autor FROM autor WHERE nome = 'Machado de Assis'", Integer.class);

        jdbc.update("""
                INSERT INTO obra (titulo, sinopse, idioma, ano_publicacao)
                VALUES ('Dom Casmurro', 'Sinopse de teste', 'Português', 1899)
                """);
        idObra = jdbc.queryForObject("SELECT id_obra FROM obra WHERE titulo = 'Dom Casmurro'", Integer.class);
        jdbc.update("INSERT INTO obra_autor (id_obra, id_autor) VALUES (?, ?)", idObra, idAutor);

        jdbc.update("""
                INSERT INTO ebook (id_obra, formato, numero_paginas, arquivo_url, valor_emprestimo)
                VALUES (?, 'EPUB', 256, '/arquivos/dom.epub', 9.90)
                """, idObra);
        idEbook = jdbc.queryForObject("SELECT id_ebook FROM ebook WHERE id_obra = ?", Integer.class, idObra);

        jdbc.update("""
                INSERT INTO audiobook (id_obra, narrador, duracao_minutos, formato, arquivo_url, valor_emprestimo)
                VALUES (?, 'Narrador Teste', 600, 'MP3', '/arquivos/dom.mp3', 14.90)
                """, idObra);
        idAudiobook = jdbc.queryForObject("SELECT id_audiobook FROM audiobook WHERE id_obra = ?", Integer.class, idObra);
    }

    protected int inserirUsuario(String nome, String email, String perfil) {
        jdbc.update("INSERT INTO usuario (nome, email, senha_hash, perfil) VALUES (?, ?, ?, ?)",
                nome, email, encoder.encode(SENHA_PADRAO), perfil);
        return jdbc.queryForObject("SELECT id_usuario FROM usuario WHERE email = ?", Integer.class, email);
    }

    protected int contar(String sql, Object... args) {
        Integer n = jdbc.queryForObject(sql, Integer.class, args);
        return n == null ? 0 : n;
    }
}
