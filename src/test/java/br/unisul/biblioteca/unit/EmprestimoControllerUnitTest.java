package br.unisul.biblioteca.unit;

import br.unisul.biblioteca.controller.EmprestimoController;
import br.unisul.biblioteca.model.Acesso;
import br.unisul.biblioteca.model.Usuario;
import br.unisul.biblioteca.repository.EmprestimoRepository;
import br.unisul.biblioteca.security.UsuarioLogado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmprestimoControllerUnitTest {

    private static final String REDIRECT = "redirect:/emprestimos";

    @Mock
    private EmprestimoRepository emprestimos;

    @InjectMocks
    private EmprestimoController controller;

    private final UsuarioLogado usuario =
            new UsuarioLogado(new Usuario(5, "Ana", "ana@x.com", "h", "usuario", true));

    @Test
    void listaOsEmprestimosDoUsuario() {
        when(emprestimos.listarPorUsuario(5)).thenReturn(List.of());
        ConcurrentModel model = new ConcurrentModel();

        assertEquals("emprestimos", controller.listar(usuario, model));
        assertTrue(model.containsAttribute("emprestimos"));
    }

    @Test
    void emprestarComSucessoMostraMensagem() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.emprestar(usuario, 7, null, "pix", redirect);

        assertEquals(REDIRECT, view);
        verify(emprestimos).realizar(5, 7, null, "pix");
        assertEquals("Empréstimo realizado! Você tem 30 dias para ler.", redirect.getFlashAttributes().get("msg"));
    }

    @Test
    void emprestarComErroDaProcedureMostraAMensagemDoBanco() {
        when(emprestimos.realizar(5, 7, null, "pix")).thenThrow(new IllegalArgumentException("Livro não encontrado"));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.emprestar(usuario, 7, null, "pix", redirect);

        assertEquals(REDIRECT, view);
        assertEquals("Livro não encontrado", redirect.getFlashAttributes().get("erro"));
    }

    @Test
    void devolverComSucesso() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.devolver(usuario, 1, redirect);

        assertEquals(REDIRECT, view);
        verify(emprestimos).devolver(1, 5);
        assertEquals("Livro devolvido. O acesso foi bloqueado.", redirect.getFlashAttributes().get("msg"));
    }

    @Test
    void devolverComErroMostraMensagem() {
        doThrow(new IllegalArgumentException("Empréstimo não encontrado.")).when(emprestimos).devolver(1, 5);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        controller.devolver(usuario, 1, redirect);

        assertEquals("Empréstimo não encontrado.", redirect.getFlashAttributes().get("erro"));
    }

    @Test
    void lerComEmprestimoAtivoAbreALeitura() {
        Acesso acesso = new Acesso(1, "Dom Casmurro", "/arquivos/dom.epub", 10.0, 5);
        when(emprestimos.acessoLiberado(1, 5)).thenReturn(Optional.of(acesso));
        ConcurrentModel model = new ConcurrentModel();

        String view = controller.ler(usuario, 1, model, new RedirectAttributesModelMap());

        assertEquals("leitura", view);
        assertEquals(acesso, model.getAttribute("acesso"));
    }

    @Test
    void lerComAcessoBloqueadoVoltaParaAListaComErro() {
        when(emprestimos.acessoLiberado(1, 5)).thenReturn(Optional.empty());
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.ler(usuario, 1, new ConcurrentModel(), redirect);

        assertEquals(REDIRECT, view);
        assertEquals("Acesso bloqueado: o empréstimo expirou ou foi devolvido.", redirect.getFlashAttributes().get("erro"));
    }

    @Test
    void salvarProgressoVoltaParaALeitura() {
        String view = controller.progresso(usuario, 1, 30.0);

        assertEquals("redirect:/emprestimos/1/ler", view);
        verify(emprestimos).salvarProgresso(1, 5, 30.0);
    }

    @Test
    void salvarProgressoPorPaginaVoltaParaALeitura() {
        String view = controller.progressoPorPagina(usuario, 1, 120);

        assertEquals("redirect:/emprestimos/1/ler", view);
        verify(emprestimos).salvarProgressoPagina(1, 5, 120);
    }
}
