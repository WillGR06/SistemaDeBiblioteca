package br.unisul.biblioteca.security;

import br.unisul.biblioteca.model.Usuario;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

/** Usuário autenticado, com o id e o nome vindos da tabela usuario. */
public class UsuarioLogado extends User {

    private final int id;
    private final String nome;

    public UsuarioLogado(Usuario u) {
        super(u.email(), u.senhaHash(), u.ativo(), true, true, true,
                List.of(new SimpleGrantedAuthority(
                        "administrador".equals(u.perfil()) ? "ROLE_ADMIN" : "ROLE_USUARIO")));
        this.id = u.id();
        this.nome = u.nome();
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
}
