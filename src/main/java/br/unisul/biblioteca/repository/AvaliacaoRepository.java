package br.unisul.biblioteca.repository;

import br.unisul.biblioteca.model.AvaliacaoResumo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AvaliacaoRepository {

    private static final String SELECT_BASE = """
            SELECT u.nome, a.nota, a.comentario, DATE_FORMAT(a.data_avaliacao, '%d/%m/%Y') AS data
              FROM avaliacao a
              JOIN usuario u ON u.id_usuario = a.id_usuario
             WHERE a.id_obra = ?
            """;

    private static final RowMapper<AvaliacaoResumo> MAPPER = (rs, i) -> new AvaliacaoResumo(
            rs.getString("nome"), rs.getInt("nota"), rs.getString("comentario"), rs.getString("data"));

    private final JdbcTemplate jdbc;

    public AvaliacaoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Cria a avaliação do usuário para a obra ou atualiza a que já existe (uma por usuário e obra). */
    public void salvar(int idUsuario, int idObra, int nota, String comentario) {
        if (nota < 1 || nota > 5) {
            throw new IllegalArgumentException("A nota deve ser de 1 a 5.");
        }
        String texto = comentario == null || comentario.isBlank() ? null : comentario.trim();
        if (texto != null && texto.length() > 1000) {
            throw new IllegalArgumentException("O comentário pode ter no máximo 1000 caracteres.");
        }
        jdbc.update("""
                INSERT INTO avaliacao (id_usuario, id_obra, nota, comentario)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE nota = VALUES(nota), comentario = VALUES(comentario),
                                        data_avaliacao = NOW()
                """, idUsuario, idObra, nota, texto);
    }

    public List<AvaliacaoResumo> listarPorObra(int idObra) {
        return jdbc.query(SELECT_BASE + " ORDER BY a.data_avaliacao DESC LIMIT 50", MAPPER, idObra);
    }

    public Optional<AvaliacaoResumo> doUsuario(int idUsuario, int idObra) {
        return jdbc.query(SELECT_BASE + " AND a.id_usuario = ?", MAPPER, idObra, idUsuario)
                .stream().findFirst();
    }
}
