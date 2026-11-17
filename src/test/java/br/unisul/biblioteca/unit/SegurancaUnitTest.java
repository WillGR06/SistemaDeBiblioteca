package br.unisul.biblioteca.unit;

import br.unisul.biblioteca.model.ObraForm;
import br.unisul.biblioteca.model.Usuario;
import br.unisul.biblioteca.repository.UsuarioRepository;
import br.unisul.biblioteca.security.UsuarioDetailsService;
import br.unisul.biblioteca.security.UsuarioLogado;
import br.unisul.biblioteca.util.Erros;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SegurancaUnitTest {

    @Mock
    private UsuarioRepository usuarios;

    @Test
    void administradorRecebePapelDeAdministrador() {
        when(usuarios.porEmail("ana@x.com")).thenReturn(
                Optional.of(new Usuario(1, "Ana", "ana@x.com", "hash", "administrador", true)));

        UsuarioLogado u = (UsuarioLogado) new UsuarioDetailsService(usuarios).loadUserByUsername(" ana@x.com ");

        assertEquals("ana@x.com", u.getUsername());
        assertEquals(1, u.getId());
        assertEquals("Ana", u.getNome());
        assertTrue(u.isEnabled());
        assertTrue(u.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void leitorRecebePapelDeUsuario() {
        when(usuarios.porEmail("leitor@x.com")).thenReturn(
                Optional.of(new Usuario(2, "Leo", "leitor@x.com", "hash", "usuario", true)));

        UsuarioLogado u = (UsuarioLogado) new UsuarioDetailsService(usuarios).loadUserByUsername("leitor@x.com");

        assertTrue(u.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USUARIO")));
    }

    @Test
    void membroInativoFicaDesabilitado() {
        when(usuarios.porEmail("off@x.com")).thenReturn(
                Optional.of(new Usuario(3, "Off", "off@x.com", "hash", "usuario", false)));

        UsuarioLogado u = (UsuarioLogado) new UsuarioDetailsService(usuarios).loadUserByUsername("off@x.com");

        assertFalse(u.isEnabled());
    }

    @Test
    void usuarioInexistenteLancaExcecao() {
        when(usuarios.porEmail("x@x.com")).thenReturn(Optional.empty());
        UsuarioDetailsService service = new UsuarioDetailsService(usuarios);

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("x@x.com"));
    }

    @Test
    void erroDeIntegridadeViraMensagemAmigavel() {
        assertEquals("Operação não permitida: o registro está em uso ou os dados são inválidos.",
                Erros.mensagem(new DataIntegrityViolationException("fk")));
    }

    @Test
    void formularioVazioTemValoresPadrao() {
        ObraForm form = ObraForm.vazio();

        assertNull(form.idObra());
        assertEquals("Português", form.idioma());
        assertEquals("EPUB", form.ebookFormato());
        assertEquals("MP3", form.audioFormato());
    }
}
