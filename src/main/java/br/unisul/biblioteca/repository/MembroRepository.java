package br.unisul.biblioteca.repository;

import br.unisul.biblioteca.model.Membro;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Gestão de membros (usuários) pelo administrador. Toda alteração fica registrada no histórico. */
@Repository
public class MembroRepository {

    private final JdbcTemplate jdbc;

    public MembroRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Membro> listar(String busca) {
        String like = "%" + (busca == null ? "" : busca.trim()) + "%";
        return jdbc.query("""
                SELECT u.id_usuario, u.nome, u.email, u.perfil, u.ativo,
                       DATE_FORMAT(u.data_cadastro, '%d/%m/%Y') AS cadastro,
                       (SELECT COUNT(*) FROM emprestimo e WHERE e.id_usuario = u.id_usuario) AS total
                  FROM usuario u
                 WHERE u.nome LIKE ? OR u.email LIKE ?
                 ORDER BY u.nome
                """,
                (rs, i) -> new Membro(
                        rs.getInt("id_usuario"),
                        rs.getString("nome"),
                        rs.getString("email"),
                        rs.getString("perfil"),
                        rs.getBoolean("ativo"),
                        rs.getString("cadastro"),
                        rs.getInt("total")),
                like, like);
    }

    @Transactional
    public void alterarStatus(int idMembro, boolean ativo, int idAdmin) {
        if (idMembro == idAdmin) {
            throw new IllegalArgumentException("Você não pode desativar a própria conta.");
        }
        String nome = nomeDe(idMembro);
        jdbc.update("UPDATE usuario SET ativo = ? WHERE id_usuario = ?", ativo, idMembro);
        registrar(idAdmin, idMembro, (ativo ? "Membro ativado: " : "Membro desativado: ") + nome);
    }

    @Transactional
    public void alterarPerfil(int idMembro, String perfil, int idAdmin) {
        if (!"usuario".equals(perfil) && !"administrador".equals(perfil)) {
            throw new IllegalArgumentException("Perfil inválido.");
        }
        if (idMembro == idAdmin) {
            throw new IllegalArgumentException("Você não pode alterar o seu próprio perfil.");
        }
        String nome = nomeDe(idMembro);
        jdbc.update("UPDATE usuario SET perfil = ? WHERE id_usuario = ?", perfil, idMembro);
        registrar(idAdmin, idMembro, "Perfil de " + nome + " alterado para " + perfil);
    }

    private String nomeDe(int idMembro) {
        Optional<String> nome = jdbc.queryForList("SELECT nome FROM usuario WHERE id_usuario = ?",
                String.class, idMembro).stream().findFirst();
        return nome.orElseThrow(() -> new IllegalArgumentException("Membro não encontrado."));
    }

    private void registrar(int idAdmin, int idMembro, String descricao) {
        jdbc.update("""
                INSERT INTO log_administracao (id_administrador, tabela_afetada, id_registro, acao, descricao)
                VALUES (?, 'usuario', ?, 'editar', ?)
                """, idAdmin, idMembro, descricao);
    }
}
