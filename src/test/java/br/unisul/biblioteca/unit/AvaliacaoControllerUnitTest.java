package br.unisul.biblioteca.unit;

import br.unisul.biblioteca.controller.AvaliacaoController;
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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvaliacaoControllerUnitTest {

    @Mock
    private AvaliacaoRepository avaliacoes;

    @Mock
    private LivroRepository livros;

    @InjectMocks
    private AvaliacaoController controller;

    private final UsuarioLogado usuario =
            new UsuarioLogado(new Usuario(5, "Ana", "ana@x.com", "h", "usuario", true));

    private Livro livro() {
        return new Livro(1, "Dom Casmurro", "Sinopse", "Português", 1899, "Machado", true, false, 0);
    }

    @Test
    void avaliarLivroInexistenteRetorna404() {
        when(livros.buscarPorId(9)).thenReturn(Optional.empty());
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        assertThrows(ResponseStatusException.class, () -> controller.avaliar(usuario, 9, 5, "x", redirect));
    }

    @Test
    void avaliarComSucessoVoltaParaOLivro() {
        when(livros.buscarPorId(1)).thenReturn(Optional.of(livro()));
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.avaliar(usuario, 1, 4, "Muito bom", redirect);

        assertEquals("redirect:/livros/1", view);
        verify(avaliacoes).salvar(5, 1, 4, "Muito bom");
        assertEquals("Avaliação salva. Obrigado!", redirect.getFlashAttributes().get("msg"));
    }

    @Test
    void notaInvalidaMostraAMensagemDeErro() {
        when(livros.buscarPorId(1)).thenReturn(Optional.of(livro()));
        doThrow(new IllegalArgumentException("A nota deve ser de 1 a 5.")).when(avaliacoes).salvar(5, 1, 9, null);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.avaliar(usuario, 1, 9, null, redirect);

        assertEquals("redirect:/livros/1", view);
        assertEquals("A nota deve ser de 1 a 5.", redirect.getFlashAttributes().get("erro"));
    }
}
