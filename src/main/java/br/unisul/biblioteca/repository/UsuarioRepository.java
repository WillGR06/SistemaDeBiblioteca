package br.unisul.biblioteca.repository;

import br.unisul.biblioteca.model.Usuario;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UsuarioRepository {

    private final JdbcTemplate jdbc;

    public UsuarioRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<Usuario> porEmail(String email) {
        return jdbc.query("""
                SELECT id_usuario, nome, email, senha_hash, perfil, ativo
                  FROM usuario WHERE email = ?
                """,
                (rs, i) -> new Usuario(rs.getInt(1), rs.getString(2), rs.getString(3),
                        rs.getString(4), rs.getString(5), rs.getBoolean(6)),
                email).stream().findFirst();
    }

    public boolean existeEmail(String email) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM usuario WHERE email = ?", Integer.class, email);
        return n != null && n > 0;
    }

    /** Cadastra um usuário comum (o perfil padrão do banco é 'usuario'). */
    public void criar(String nome, String email, String senhaHash) {
        jdbc.update("INSERT INTO usuario (nome, email, senha_hash) VALUES (?, ?, ?)", nome, email, senhaHash);
    }
}
