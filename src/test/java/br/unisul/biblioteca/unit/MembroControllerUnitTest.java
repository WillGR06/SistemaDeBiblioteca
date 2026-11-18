package br.unisul.biblioteca.unit;

import br.unisul.biblioteca.controller.MembroController;
import br.unisul.biblioteca.model.Usuario;
import br.unisul.biblioteca.repository.MembroRepository;
import br.unisul.biblioteca.security.UsuarioLogado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembroControllerUnitTest {

    private static final String REDIRECT = "redirect:/admin/membros";

    @Mock
    private MembroRepository membros;

    @InjectMocks
    private MembroController controller;

    private final UsuarioLogado admin =
            new UsuarioLogado(new Usuario(1, "Admin", "admin@x.com", "h", "administrador", true));

    @Test
    void listaOsMembrosEInformaOIdDoAdministrador() {
        when(membros.listar("ana")).thenReturn(List.of());
        ConcurrentModel model = new ConcurrentModel();

        String view = controller.listar(admin, "ana", model);

        assertEquals("admin/membros", view);
        assertEquals(1, model.getAttribute("meuId"));
        assertEquals("ana", model.getAttribute("busca"));
    }

    @Test
    void desativarMembroMostraMensagem() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.status(admin, 7, false, redirect);

        assertEquals(REDIRECT, view);
        verify(membros).alterarStatus(7, false, 1);
        assertEquals("Membro desativado.", redirect.getFlashAttributes().get("msg"));
    }

    @Test
    void ativarMembroMostraMensagem() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        controller.status(admin, 7, true, redirect);

        assertEquals("Membro ativado.", redirect.getFlashAttributes().get("msg"));
    }

    @Test
    void erroAoAlterarStatusMostraMensagem() {
        doThrow(new IllegalArgumentException("Você não pode desativar a própria conta."))
                .when(membros).alterarStatus(1, false, 1);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        controller.status(admin, 1, false, redirect);

        assertEquals("Você não pode desativar a própria conta.", redirect.getFlashAttributes().get("erro"));
    }

    @Test
    void alterarPerfilComSucesso() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.perfil(admin, 7, "administrador", redirect);

        assertEquals(REDIRECT, view);
        verify(membros).alterarPerfil(7, "administrador", 1);
        assertEquals("Perfil alterado.", redirect.getFlashAttributes().get("msg"));
    }

    @Test
    void erroAoAlterarPerfilMostraMensagem() {
        doThrow(new IllegalArgumentException("Perfil inválido.")).when(membros).alterarPerfil(7, "xyz", 1);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        controller.perfil(admin, 7, "xyz", redirect);

        assertEquals("Perfil inválido.", redirect.getFlashAttributes().get("erro"));
    }
}
