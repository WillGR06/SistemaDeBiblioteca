package br.unisul.biblioteca.repository;

import br.unisul.biblioteca.model.Acesso;
import br.unisul.biblioteca.model.EmprestimoResumo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;
import java.util.List;
import java.util.Optional;

@Repository
public class EmprestimoRepository {

    private final JdbcTemplate jdbc;

    public EmprestimoRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Chama a procedure sp_realizar_emprestimo e devolve o id do empréstimo criado. */
    public int realizar(int idUsuario, Integer idEbook, Integer idAudiobook, String forma) {
        Integer id = jdbc.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall("{CALL sp_realizar_emprestimo(?, ?, ?, ?, ?)}")) {
                cs.setInt(1, idUsuario);
                if (idEbook == null) cs.setNull(2, Types.INTEGER); else cs.setInt(2, idEbook);
                if (idAudiobook == null) cs.setNull(3, Types.INTEGER); else cs.setInt(3, idAudiobook);
                cs.setString(4, forma);
                cs.registerOutParameter(5, Types.INTEGER);
                cs.execute();
                return cs.getInt(5);
            }
        });
        return id == null ? 0 : id;
    }

    /** Devolução antecipada (só o dono do empréstimo pode devolver). */
    public void devolver(int idEmprestimo, int idUsuario) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM emprestimo WHERE id_emprestimo = ? AND id_usuario = ?",
                Integer.class, idEmprestimo, idUsuario);
        if (n == null || n == 0) {
            throw new IllegalArgumentException("Empréstimo não encontrado.");
        }
        jdbc.execute((Connection con) -> {
            try (CallableStatement cs = con.prepareCall("{CALL sp_devolver_emprestimo(?)}")) {
                cs.setInt(1, idEmprestimo);
                cs.execute();
            }
            return null;
        });
    }

    public List<EmprestimoResumo> listarPorUsuario(int idUsuario) {
        return jdbc.query("""
                SELECT e.id_emprestimo,
                       COALESCE(oe.titulo, oa.titulo) AS titulo,
                       CASE WHEN e.id_ebook IS NOT NULL THEN 'E-book' ELSE 'Audiobook' END AS tipo,
                       DATE_FORMAT(e.data_emprestimo, '%d/%m/%Y %H:%i') AS emprestado_em,
                       DATE_FORMAT(e.data_vencimento, '%d/%m/%Y %H:%i') AS vencimento,
                       DATE_FORMAT(e.data_bloqueio, '%d/%m/%Y %H:%i') AS bloqueado_em,
                       DATE_FORMAT(e.data_devolucao, '%d/%m/%Y %H:%i') AS devolvido_em,
                       CASE
                         WHEN e.status = 'devolvido' THEN 'Devolvido'
                         WHEN e.status = 'ativo' AND e.data_vencimento >= NOW() THEN 'Emprestado'
                         ELSE 'Bloqueado'
                       END AS situacao,
                       fn_dias_restantes(e.id_emprestimo) AS dias_restantes
                  FROM emprestimo e
                  LEFT JOIN ebook eb ON eb.id_ebook = e.id_ebook
                  LEFT JOIN obra oe ON oe.id_obra = eb.id_obra
                  LEFT JOIN audiobook ab ON ab.id_audiobook = e.id_audiobook
                  LEFT JOIN obra oa ON oa.id_obra = ab.id_obra
                 WHERE e.id_usuario = ?
                 ORDER BY e.data_emprestimo DESC
                """,
                (rs, i) -> new EmprestimoResumo(
                        rs.getInt("id_emprestimo"),
                        rs.getString("titulo"),
                        rs.getString("tipo"),
                        rs.getString("emprestado_em"),
                        rs.getString("vencimento"),
                        rs.getString("bloqueado_em"),
                        rs.getString("devolvido_em"),
                        rs.getString("situacao"),
                        rs.getInt("dias_restantes")),
                idUsuario);
    }

    /** Só retorna dados se o empréstimo for do usuário, estiver ativo e dentro do prazo. */
    public Optional<Acesso> acessoLiberado(int idEmprestimo, int idUsuario) {
        return jdbc.query("""
                SELECT e.id_emprestimo,
                       COALESCE(oe.titulo, oa.titulo) AS titulo,
                       COALESCE(eb.arquivo_url, ab.arquivo_url) AS url,
                       COALESCE(ac.progresso_percentual, 0) AS progresso,
                       eb.numero_paginas AS total_paginas,
                       fn_dias_restantes(e.id_emprestimo) AS dias_restantes
                  FROM emprestimo e
                  LEFT JOIN ebook eb ON eb.id_ebook = e.id_ebook
                  LEFT JOIN obra oe ON oe.id_obra = eb.id_obra
                  LEFT JOIN audiobook ab ON ab.id_audiobook = e.id_audiobook
                  LEFT JOIN obra oa ON oa.id_obra = ab.id_obra
                  LEFT JOIN acesso_digital ac ON ac.id_emprestimo = e.id_emprestimo
                 WHERE e.id_emprestimo = ? AND e.id_usuario = ?
                   AND e.status = 'ativo' AND e.data_vencimento >= NOW()
                 LIMIT 1
                """,
                (rs, i) -> {
                    double progresso = rs.getDouble("progresso");
                    int total = rs.getInt("total_paginas");
                    boolean semPaginas = rs.wasNull() || total <= 0;
                    return new Acesso(
                            rs.getInt("id_emprestimo"),
                            rs.getString("titulo"),
                            rs.getString("url"),
                            progresso,
                            rs.getInt("dias_restantes"),
                            semPaginas ? null : total,
                            semPaginas ? 0 : (int) Math.round(progresso * total / 100.0));
                },
                idEmprestimo, idUsuario).stream().findFirst();
    }

    /**
     * Salva o progresso como número da página (de 0 até o total de páginas do e-book).
     * Internamente continua gravando o percentual. Obras sem número de páginas são ignoradas.
     */
    public void salvarProgressoPagina(int idEmprestimo, int idUsuario, int pagina) {
        jdbc.update("""
                UPDATE acesso_digital ac
                  JOIN emprestimo e ON e.id_emprestimo = ac.id_emprestimo
                  JOIN ebook eb ON eb.id_ebook = e.id_ebook
                   SET ac.progresso_percentual = ROUND(LEAST(GREATEST(?, 0), eb.numero_paginas) * 100 / eb.numero_paginas, 2),
                       ac.data_acesso = NOW()
                 WHERE ac.id_emprestimo = ? AND e.id_usuario = ?
                   AND eb.numero_paginas > 0
                   AND e.status = 'ativo' AND e.data_vencimento >= NOW()
                """, pagina, idEmprestimo, idUsuario);
    }

    public void salvarProgresso(int idEmprestimo, int idUsuario, double percentual) {
        double valor = Math.max(0, Math.min(100, percentual));
        jdbc.update("""
                UPDATE acesso_digital ac
                  JOIN emprestimo e ON e.id_emprestimo = ac.id_emprestimo
                   SET ac.progresso_percentual = ?, ac.data_acesso = NOW()
                 WHERE ac.id_emprestimo = ? AND e.id_usuario = ?
                   AND e.status = 'ativo' AND e.data_vencimento >= NOW()
                """, valor, idEmprestimo, idUsuario);
    }
}
