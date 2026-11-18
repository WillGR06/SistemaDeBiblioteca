package br.unisul.biblioteca.unit;

import br.unisul.biblioteca.controller.AdminController;
import br.unisul.biblioteca.model.ObraForm;
import br.unisul.biblioteca.model.Usuario;
import br.unisul.biblioteca.repository.AdminRepository;
import br.unisul.biblioteca.repository.LivroRepository;
import br.unisul.biblioteca.security.UsuarioLogado;
import br.unisul.biblioteca.util.ArmazenamentoCapas;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminControllerUnitTest {

    private static final String FORM = "admin/obra-form";

    @Mock
    private AdminRepository admin;

    @Mock
    private LivroRepository livros;

    @Mock
    private ArmazenamentoCapas capas;

    @InjectMocks
    private AdminController controller;

    private final UsuarioLogado adminLogado =
            new UsuarioLogado(new Usuario(1, "Admin", "admin@x.com", "h", "administrador", true));

    private ObraForm formComTitulo(Integer id, String titulo) {
        return new ObraForm(id, titulo, "", "Português", null, null, "", "EPUB", "", null, null,
                "", null, "MP3", "", null);
    }

    @Test
    void listaAsObras() {
        when(livros.buscar(null)).thenReturn(List.of());

        assertEquals("admin/obras", controller.listar(new ConcurrentModel()));
    }

    @Test
    void novaObraMostraOFormularioVazio() {
        ConcurrentModel model = new ConcurrentModel();

        String view = controller.nova(model);

        assertEquals(FORM, view);
        assertEquals("Nova obra", model.getAttribute("tituloPagina"));
        assertEquals(ObraForm.vazio(), model.getAttribute("form"));
    }

    @Test
    void editarObraInexistenteRetorna404() {
        when(admin.carregar(9)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> controller.editar(9, new ConcurrentModel()));
    }

    @Test
    void editarObraExistenteMostraOFormulario() {
        ObraForm form = formComTitulo(1, "Dom Casmurro");
        when(admin.carregar(1)).thenReturn(Optional.of(form));
        ConcurrentModel model = new ConcurrentModel();

        String view = controller.editar(1, model);

        assertEquals(FORM, view);
        assertEquals("Editar obra", model.getAttribute("tituloPagina"));
        assertEquals(form, model.getAttribute("form"));
    }

    @Test
    void salvarSemTituloMostraErro() {
        ConcurrentModel model = new ConcurrentModel();

        String view = controller.salvar(adminLogado, formComTitulo(null, "  "), null, model,
                new RedirectAttributesModelMap());

        assertEquals(FORM, view);
        assertEquals("Informe o título da obra.", model.getAttribute("erro"));
    }

    @Test
    void salvarComSucessoVoltaParaALista() {
        ObraForm form = formComTitulo(null, "Livro Novo");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.salvar(adminLogado, form, null, new ConcurrentModel(), redirect);

        assertEquals("redirect:/admin/obras", view);
        verify(admin).salvar(form, 1, null);
        assertEquals("Obra salva com sucesso.", redirect.getFlashAttributes().get("msg"));
    }

    @Test
    void salvarComFalhaMostraMensagemAmigavel() {
        ObraForm form = formComTitulo(2, "Livro");
        when(admin.salvar(form, 1, null)).thenThrow(new RuntimeException("falha"));
        ConcurrentModel model = new ConcurrentModel();

        String view = controller.salvar(adminLogado, form, null, model, new RedirectAttributesModelMap());

        assertEquals(FORM, view);
        assertEquals("Não foi possível concluir a operação. Tente novamente.", model.getAttribute("erro"));
        assertEquals("Editar obra", model.getAttribute("tituloPagina"));
    }

    @Test
    void excluirComSucesso() {
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.excluir(adminLogado, 3, redirect);

        assertEquals("redirect:/admin/obras", view);
        verify(admin).excluir(3, 1);
        assertEquals("Obra excluída.", redirect.getFlashAttributes().get("msg"));
    }

    @Test
    void excluirObraComEmprestimosMostraErro() {
        doThrow(new RuntimeException("fk")).when(admin).excluir(3, 1);
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        controller.excluir(adminLogado, 3, redirect);

        assertEquals("Não é possível excluir: existem empréstimos desta obra.",
                redirect.getFlashAttributes().get("erro"));
    }

    @Test
    void mostraOHistoricoDeAcoes() {
        when(admin.logRecente()).thenReturn(List.of());

        assertEquals("admin/log", controller.log(new ConcurrentModel()));
    }

    @Test
    void novaEditoraValidaRetornaIdENome() {
        when(admin.criarEditora("Companhia das Letras", "Brasil")).thenReturn(7);

        var resposta = controller.novaEditora(" Companhia das Letras ", "Brasil");

        assertEquals(200, resposta.getStatusCode().value());
        assertEquals(7, resposta.getBody().get("id"));
        assertEquals("Companhia das Letras", resposta.getBody().get("nome"));
    }

    @Test
    void novaEditoraSemNomeRetornaErro() {
        var resposta = controller.novaEditora("   ", null);

        assertEquals(400, resposta.getStatusCode().value());
        assertEquals("Informe o nome da editora.", resposta.getBody().get("erro"));
    }

    @Test
    void novaEditoraComNomeMuitoLongoRetornaErro() {
        var resposta = controller.novaEditora("x".repeat(121), null);

        assertEquals(400, resposta.getStatusCode().value());
    }

    @Test
    void salvarComCapaGravaAImagemEApagaACapaAntiga() {
        ObraForm form = formComTitulo(2, "Livro");
        MockMultipartFile imagem = new MockMultipartFile("capa", "x.png", "image/png", new byte[]{1});
        when(capas.salvar(imagem)).thenReturn("/capas/nova.png");
        when(admin.capaUrl(2)).thenReturn("/capas/antiga.png");

        String view = controller.salvar(adminLogado, form, imagem, new ConcurrentModel(),
                new RedirectAttributesModelMap());

        assertEquals("redirect:/admin/obras", view);
        verify(admin).salvar(form, 1, "/capas/nova.png");
        verify(capas).apagar("/capas/antiga.png");
    }

    @Test
    void salvarComImagemInvalidaMostraErroENaoSalvaAObra() {
        ObraForm form = formComTitulo(null, "Livro");
        MockMultipartFile imagem = new MockMultipartFile("capa", "x.txt", "text/plain", new byte[]{1});
        when(capas.salvar(imagem)).thenThrow(new IllegalArgumentException("Envie uma imagem JPG, PNG, GIF ou WEBP."));
        ConcurrentModel model = new ConcurrentModel();

        String view = controller.salvar(adminLogado, form, imagem, model, new RedirectAttributesModelMap());

        assertEquals(FORM, view);
        assertEquals("Envie uma imagem JPG, PNG, GIF ou WEBP.", model.getAttribute("erro"));
        org.mockito.Mockito.verify(admin, org.mockito.Mockito.never()).salvar(form, 1, null);
    }

    @Test
    void falhaAoSalvarApagaAImagemRecemEnviada() {
        ObraForm form = formComTitulo(null, "Livro");
        MockMultipartFile imagem = new MockMultipartFile("capa", "x.png", "image/png", new byte[]{1});
        when(capas.salvar(imagem)).thenReturn("/capas/nova.png");
        when(admin.salvar(form, 1, "/capas/nova.png")).thenThrow(new RuntimeException("falha"));

        controller.salvar(adminLogado, form, imagem, new ConcurrentModel(), new RedirectAttributesModelMap());

        verify(capas).apagar("/capas/nova.png");
    }
}
