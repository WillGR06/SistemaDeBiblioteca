package br.unisul.biblioteca.unit;

import br.unisul.biblioteca.controller.LivroController;
import br.unisul.biblioteca.model.AvaliacaoResumo;
import br.unisul.biblioteca.model.Livro;
import br.unisul.biblioteca.model.Usuario;
import br.unisul.biblioteca.repository.AvaliacaoRepository;
import br.unisul.biblioteca.repository.LivroRepository;
import br.unisul.biblioteca.security.UsuarioLogado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LivroControllerUnitTest {

    @Mock
    private LivroRepository livros;

    @Mock
    private AvaliacaoRepository avaliacoes;

    @InjectMocks
    private LivroController controller;

    private Livro livro() {
        return new Livro(1, "Dom Casmurro", "Sinopse", "Português", 1899, "Machado de Assis", true, false, 4.5);
    }

    @Test
    void catalogoListaOsLivrosDaBusca() {
        when(livros.buscar("dom")).thenReturn(List.of(livro()));
        ConcurrentModel model = new ConcurrentModel();

        String view = controller.catalogo("dom", model);

        assertEquals("catalogo", view);
        assertEquals(List.of(livro()), model.getAttribute("livros"));
        assertEquals("dom", model.getAttribute("busca"));
    }

    @Test
    void detalheDeLivroInexistenteRetorna404() {
        when(livros.buscarPorId(9)).thenReturn(Optional.empty());

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> controller.detalhe(9, null, new ConcurrentModel()));

        assertEquals(404, erro.getStatusCode().value());
    }

    @Test
    void detalheParaVisitanteNaoTraAAvaliacaoPropria() {
        when(livros.buscarPorId(1)).thenReturn(Optional.of(livro()));
        when(livros.formatosDaObra(1)).thenReturn(List.of());
        when(avaliacoes.listarPorObra(1)).thenReturn(List.of());
        ConcurrentModel model = new ConcurrentModel();

        String view = controller.detalhe(1, null, model);

        assertEquals("livro", view);
        assertTrue(model.containsAttribute("livro"));
        assertTrue(model.containsAttribute("formatos"));
        assertTrue(model.containsAttribute("avaliacoes"));
        assertFalse(model.containsAttribute("minha"));
    }

    @Test
    void detalheParaUsuarioLogadoTrazASuaAvaliacao() {
        UsuarioLogado usuario = new UsuarioLogado(new Usuario(5, "Ana", "ana@x.com", "h", "usuario", true));
        AvaliacaoResumo minha = new AvaliacaoResumo("Ana", 5, "Ótimo", "01/01/2026");
        when(livros.buscarPorId(1)).thenReturn(Optional.of(livro()));
        when(livros.formatosDaObra(1)).thenReturn(List.of());
        when(avaliacoes.listarPorObra(1)).thenReturn(List.of(minha));
        when(avaliacoes.doUsuario(5, 1)).thenReturn(Optional.of(minha));
        ConcurrentModel model = new ConcurrentModel();

        controller.detalhe(1, usuario, model);

        assertEquals(minha, model.getAttribute("minha"));
    }
}
