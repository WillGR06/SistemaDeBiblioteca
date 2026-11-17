package br.unisul.biblioteca;

import br.unisul.biblioteca.model.Usuario;
import br.unisul.biblioteca.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioIntegracaoTest extends BaseIntegracaoTest {

    @Autowired
    private UsuarioRepository usuarios;

    @Test
    void usuarioNovoNasceComPerfilComumEAtivo() {
        usuarios.criar("Novo Usuário", "novo@teste.com", encoder.encode("abc123"));

        Usuario u = usuarios.porEmail("novo@teste.com").orElseThrow();

        assertEquals("usuario", u.perfil());
        assertTrue(u.ativo());
        assertTrue(encoder.matches("abc123", u.senhaHash()));
    }

    @Test
    void emailDuplicadoEhRecusado() {
        assertThrows(DuplicateKeyException.class,
                () -> usuarios.criar("Outro", "leitor@teste.com", encoder.encode("abc123")));
    }

    @Test
    void existeEmailDiferenciaCadastradoDeNaoCadastrado() {
        assertTrue(usuarios.existeEmail("leitor@teste.com"));
        assertFalse(usuarios.existeEmail("ninguem@teste.com"));
    }

    @Test
    void administradorTemPerfilAdministrador() {
        assertEquals("administrador", usuarios.porEmail("admin@teste.com").orElseThrow().perfil());
    }
}
