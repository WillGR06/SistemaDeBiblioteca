package br.unisul.biblioteca.repository;

import br.unisul.biblioteca.model.ObraForm;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Operações da área do administrador.
 * Os métodos de escrita são @Transactional para que o "SET @id_admin" e as alterações
 * usem a MESMA conexão (as triggers de auditoria leem essa variável).
 */
@Repository
public class AdminRepository {

    private static final String SQL_DEFINIR_ADMIN = "SET @id_admin = ?";
    private static final String SQL_LIMPAR_ADMIN = "SET @id_admin = NULL";

    private final JdbcTemplate jdbc;

    public AdminRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> editoras() {
        return jdbc.queryForList("SELECT id_editora, nome FROM editora ORDER BY nome");
    }

    /** Cadastra uma editora e retorna o id. Se já existir uma com o mesmo nome, reaproveita a existente. */
    @Transactional
    public int criarEditora(String nome, String pais) {
        String nomeLimpo = nome.trim();
        List<Integer> existentes = jdbc.queryForList(
                "SELECT id_editora FROM editora WHERE nome = ? LIMIT 1", Integer.class, nomeLimpo);
        if (!existentes.isEmpty()) {
            return existentes.get(0);
        }
        jdbc.update("INSERT INTO editora (nome, pais) VALUES (?, ?)", nomeLimpo, vazioParaNulo(pais));
        return ultimoId();
    }

    public List<Map<String, Object>> logRecente() {
        return jdbc.queryForList("""
                SELECT DATE_FORMAT(l.data_acao, '%d/%m/%Y %H:%i') AS data_acao,
                       u.nome AS administrador, l.tabela_afetada, l.acao, l.descricao
                  FROM log_administracao l
                  JOIN usuario u ON u.id_usuario = l.id_administrador
                 ORDER BY l.id_log DESC
                 LIMIT 50
                """);
    }

    public Optional<ObraForm> carregar(int idObra) {
        List<Map<String, Object>> obras = jdbc.queryForList("""
                SELECT o.id_obra, o.titulo, o.sinopse, o.idioma, o.ano_publicacao, o.id_editora,
                       (SELECT a.nome FROM obra_autor oa JOIN autor a ON a.id_autor = oa.id_autor
                         WHERE oa.id_obra = o.id_obra LIMIT 1) AS autor
                  FROM obra o WHERE o.id_obra = ?
                """, idObra);
        if (obras.isEmpty()) {
            return Optional.empty();
        }
        Map<String, Object> o = obras.get(0);

        List<Map<String, Object>> ebooks = jdbc.queryForList("""
                SELECT CAST(formato AS CHAR) AS formato, arquivo_url, valor_emprestimo, numero_paginas
                  FROM ebook WHERE id_obra = ? LIMIT 1
                """, idObra);
        List<Map<String, Object>> audios = jdbc.queryForList("""
                SELECT narrador, duracao_minutos, CAST(formato AS CHAR) AS formato, arquivo_url, valor_emprestimo
                  FROM audiobook WHERE id_obra = ? LIMIT 1
                """, idObra);
        Map<String, Object> eb = ebooks.isEmpty() ? Collections.<String, Object>emptyMap() : ebooks.get(0);
        Map<String, Object> au = audios.isEmpty() ? Collections.<String, Object>emptyMap() : audios.get(0);

        return Optional.of(new ObraForm(
                idObra,
                texto(o.get("titulo")), texto(o.get("sinopse")), texto(o.get("idioma")),
                inteiro(o.get("ano_publicacao")), inteiro(o.get("id_editora")), texto(o.get("autor")),
                eb.isEmpty() ? "EPUB" : texto(eb.get("formato")),
                texto(eb.get("arquivo_url")), decimal(eb.get("valor_emprestimo")), inteiro(eb.get("numero_paginas")),
                texto(au.get("narrador")), inteiro(au.get("duracao_minutos")),
                au.isEmpty() ? "MP3" : texto(au.get("formato")),
                texto(au.get("arquivo_url")), decimal(au.get("valor_emprestimo"))));
    }

    /** Endereço da capa de uma obra (ou null se não tiver). */
    public String capaUrl(int idObra) {
        List<String> capas = jdbc.queryForList("SELECT capa_url FROM obra WHERE id_obra = ?", String.class, idObra);
        return capas.isEmpty() ? null : capas.get(0);
    }

    /** Cadastra ou edita uma obra (com autor, e-book e audiobook) e retorna o id da obra. */
    @Transactional
    public int salvar(ObraForm f, int idAdmin) {
        return salvar(f, idAdmin, null);
    }

    /** Igual ao salvar acima, mas também grava a capa. Com capaUrl nulo, a capa atual é mantida. */
    @Transactional
    public int salvar(ObraForm f, int idAdmin, String capaUrl) {
        jdbc.update(SQL_DEFINIR_ADMIN, idAdmin);
        try {
            int idObra = salvarDadosDaObra(f, capaUrl);
            salvarAutor(idObra, f.autor());
            salvarEbook(idObra, f);
            salvarAudiobook(idObra, f);
            return idObra;
        } finally {
            jdbc.update(SQL_LIMPAR_ADMIN);
        }
    }

    @Transactional
    public void excluir(int idObra, int idAdmin) {
        jdbc.update(SQL_DEFINIR_ADMIN, idAdmin);
        try {
            jdbc.update("DELETE FROM obra WHERE id_obra = ?", idObra);
        } finally {
            jdbc.update(SQL_LIMPAR_ADMIN);
        }
    }

    // ---------- etapas do salvar ----------

    private int salvarDadosDaObra(ObraForm f, String capaUrl) {
        if (f.idObra() == null) {
            jdbc.update("""
                    INSERT INTO obra (titulo, sinopse, idioma, ano_publicacao, id_editora, capa_url)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """, f.titulo().trim(), vazioParaNulo(f.sinopse()), f.idioma(), f.anoPublicacao(),
                    f.idEditora(), capaUrl);
            return ultimoId();
        }
        jdbc.update("""
                UPDATE obra SET titulo = ?, sinopse = ?, idioma = ?, ano_publicacao = ?, id_editora = ?,
                       capa_url = COALESCE(?, capa_url)
                 WHERE id_obra = ?
                """, f.titulo().trim(), vazioParaNulo(f.sinopse()), f.idioma(), f.anoPublicacao(),
                f.idEditora(), capaUrl, f.idObra());
        return f.idObra();
    }

    private void salvarAutor(int idObra, String autor) {
        if (!preenchido(autor)) {
            return;
        }
        String nome = autor.trim();
        List<Integer> existentes = jdbc.queryForList(
                "SELECT id_autor FROM autor WHERE nome = ? LIMIT 1", Integer.class, nome);
        int idAutor;
        if (existentes.isEmpty()) {
            jdbc.update("INSERT INTO autor (nome) VALUES (?)", nome);
            idAutor = ultimoId();
        } else {
            idAutor = existentes.get(0);
        }
        jdbc.update("DELETE FROM obra_autor WHERE id_obra = ?", idObra);
        jdbc.update("INSERT INTO obra_autor (id_obra, id_autor) VALUES (?, ?)", idObra, idAutor);
    }

    private void salvarEbook(int idObra, ObraForm f) {
        if (!preenchido(f.ebookUrl())) {
            return;
        }
        BigDecimal valor = f.ebookValor() == null ? BigDecimal.ZERO : f.ebookValor();
        List<Integer> ids = jdbc.queryForList(
                "SELECT id_ebook FROM ebook WHERE id_obra = ? LIMIT 1", Integer.class, idObra);
        if (ids.isEmpty()) {
            jdbc.update("""
                    INSERT INTO ebook (id_obra, formato, numero_paginas, arquivo_url, valor_emprestimo)
                    VALUES (?, ?, ?, ?, ?)
                    """, idObra, f.ebookFormato(), f.ebookPaginas(), f.ebookUrl().trim(), valor);
        } else {
            jdbc.update("""
                    UPDATE ebook SET formato = ?, numero_paginas = ?, arquivo_url = ?, valor_emprestimo = ?
                     WHERE id_ebook = ?
                    """, f.ebookFormato(), f.ebookPaginas(), f.ebookUrl().trim(), valor, ids.get(0));
        }
    }

    private void salvarAudiobook(int idObra, ObraForm f) {
        if (!preenchido(f.audioUrl())) {
            return;
        }
        BigDecimal valor = f.audioValor() == null ? BigDecimal.ZERO : f.audioValor();
        List<Integer> ids = jdbc.queryForList(
                "SELECT id_audiobook FROM audiobook WHERE id_obra = ? LIMIT 1", Integer.class, idObra);
        if (ids.isEmpty()) {
            jdbc.update("""
                    INSERT INTO audiobook (id_obra, narrador, duracao_minutos, formato, arquivo_url, valor_emprestimo)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """, idObra, vazioParaNulo(f.audioNarrador()), f.audioDuracao(), f.audioFormato(),
                    f.audioUrl().trim(), valor);
        } else {
            jdbc.update("""
                    UPDATE audiobook SET narrador = ?, duracao_minutos = ?, formato = ?, arquivo_url = ?,
                           valor_emprestimo = ?
                     WHERE id_audiobook = ?
                    """, vazioParaNulo(f.audioNarrador()), f.audioDuracao(), f.audioFormato(),
                    f.audioUrl().trim(), valor, ids.get(0));
        }
    }

    // ---------- auxiliares ----------

    private int ultimoId() {
        Integer id = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Integer.class);
        return id == null ? 0 : id;
    }

    private static boolean preenchido(String s) {
        return s != null && !s.isBlank();
    }

    private static String vazioParaNulo(String s) {
        return preenchido(s) ? s.trim() : null;
    }

    private static String texto(Object o) {
        return o == null ? "" : o.toString();
    }

    private static Integer inteiro(Object o) {
        return o instanceof Number n ? n.intValue() : null;
    }

    private static BigDecimal decimal(Object o) {
        return o instanceof Number n ? new BigDecimal(n.toString()) : null;
    }
}
