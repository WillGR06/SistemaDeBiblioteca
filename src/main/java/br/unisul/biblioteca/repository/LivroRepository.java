package br.unisul.biblioteca.repository;

import br.unisul.biblioteca.model.Formato;
import br.unisul.biblioteca.model.Livro;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class LivroRepository {

    private static final String SELECT_BASE = """
            SELECT o.id_obra, o.titulo, o.sinopse, o.idioma, o.ano_publicacao, o.capa_url,
                   (SELECT GROUP_CONCAT(a.nome SEPARATOR ', ')
                      FROM obra_autor oa
                      JOIN autor a ON a.id_autor = oa.id_autor
                     WHERE oa.id_obra = o.id_obra) AS autores,
                   EXISTS (SELECT 1 FROM ebook e WHERE e.id_obra = o.id_obra) AS tem_ebook,
                   EXISTS (SELECT 1 FROM audiobook b WHERE b.id_obra = o.id_obra) AS tem_audiobook,
                   fn_media_avaliacao(o.id_obra) AS media
              FROM obra o
            """;

    private static final RowMapper<Livro> MAPPER = (rs, i) -> {
        int ano = rs.getInt("ano_publicacao");
        Integer anoPublicacao = rs.wasNull() ? null : ano;
        return new Livro(
                rs.getInt("id_obra"),
                rs.getString("titulo"),
                rs.getString("sinopse"),
                rs.getString("idioma"),
                anoPublicacao,
                rs.getString("autores"),
                rs.getBoolean("tem_ebook"),
                rs.getBoolean("tem_audiobook"),
                rs.getDouble("media"),
                rs.getString("capa_url"));
    };

    private final JdbcTemplate jdbc;

    public LivroRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Busca obras pelo título ou pelo nome do autor. Busca vazia lista tudo. */
    public List<Livro> buscar(String busca) {
        String like = "%" + (busca == null ? "" : busca.trim()) + "%";
        String sql = SELECT_BASE + """
                 WHERE o.titulo LIKE ?
                    OR EXISTS (SELECT 1
                                 FROM obra_autor oa2
                                 JOIN autor a2 ON a2.id_autor = oa2.id_autor
                                WHERE oa2.id_obra = o.id_obra AND a2.nome LIKE ?)
                 ORDER BY o.titulo
                """;
        return jdbc.query(sql, MAPPER, like, like);
    }

    public Optional<Livro> buscarPorId(int idObra) {
        return jdbc.query(SELECT_BASE + " WHERE o.id_obra = ?", MAPPER, idObra).stream().findFirst();
    }

    /** E-books e audiobooks disponíveis para empréstimo de uma obra. */
    public List<Formato> formatosDaObra(int idObra) {
        return jdbc.query("""
                SELECT 'ebook' AS tipo, id_ebook AS id, CAST(formato AS CHAR) AS formato, valor_emprestimo
                  FROM ebook WHERE id_obra = ?
                UNION ALL
                SELECT 'audiobook', id_audiobook, CAST(formato AS CHAR), valor_emprestimo
                  FROM audiobook WHERE id_obra = ?
                """,
                (rs, i) -> new Formato(rs.getString("tipo"), rs.getInt("id"),
                        rs.getString("formato"), rs.getBigDecimal("valor_emprestimo")),
                idObra, idObra);
    }
}
