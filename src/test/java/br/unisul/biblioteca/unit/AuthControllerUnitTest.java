package br.unisul.biblioteca.unit;

import br.unisul.biblioteca.controller.AuthController;
import br.unisul.biblioteca.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.ConcurrentModel;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerUnitTest {

    @Mock
    private UsuarioRepository usuarios;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private AuthController controller;

    @Test
    void mostraAsPaginasDeLoginECadastro() {
        assertEquals("login", controller.login());
        assertEquals("cadastro", controller.cadastro());
    }

    @Test
    void nomeEmBrancoEhRecusado() {
        ConcurrentModel model = new ConcurrentModel();

        String view = controller.cadastrar("  ", "a@b.com", "abc123", "abc123", model, new RedirectAttributesModelMap());

        assertEquals("cadastro", view);
        assertEquals("Informe seu nome.", model.getAttribute("erro"));
        verifyNoInteractions(usuarios, encoder);
    }

    @Test
    void emailSemArrobaEhRecusado() {
        ConcurrentModel model = new ConcurrentModel();

        controller.cadastrar("Ana", "email-invalido", "abc123", "abc123", model, new RedirectAttributesModelMap());

        assertEquals("Informe um e-mail válido.", model.getAttribute("erro"));
    }

    @Test
    void senhaCurtaEhRecusada() {
        ConcurrentModel model = new ConcurrentModel();

        controller.cadastrar("Ana", "a@b.com", "123", "123", model, new RedirectAttributesModelMap());

        assertEquals("A senha deve ter pelo menos 6 caracteres.", model.getAttribute("erro"));
    }

    @Test
    void senhasDiferentesSaoRecusadas() {
        ConcurrentModel model = new ConcurrentModel();

        controller.cadastrar("Ana", "a@b.com", "abc123", "outra123", model, new RedirectAttributesModelMap());

        assertEquals("As senhas não conferem.", model.getAttribute("erro"));
    }

    @Test
    void emailJaCadastradoEhRecusado() {
        when(usuarios.existeEmail("a@b.com")).thenReturn(true);
        ConcurrentModel model = new ConcurrentModel();

        String view = controller.cadastrar("Ana", "a@b.com", "abc123", "abc123", model, new RedirectAttributesModelMap());

        assertEquals("cadastro", view);
        assertEquals("Este e-mail já está cadastrado.", model.getAttribute("erro"));
        verify(usuarios, never()).criar(any(), any(), any());
    }

    @Test
    void cadastroValidoCriaOUsuarioComSenhaCriptografada() {
        when(usuarios.existeEmail("fulano@x.com")).thenReturn(false);
        when(encoder.encode("abc123")).thenReturn("HASH");
        RedirectAttributesModelMap redirect = new RedirectAttributesModelMap();

        String view = controller.cadastrar(" Fulano ", " Fulano@X.com ", "abc123", "abc123",
                new ConcurrentModel(), redirect);

        assertEquals("redirect:/login", view);
        verify(usuarios).criar("Fulano", "fulano@x.com", "HASH");
        assertEquals("Cadastro realizado! Faça login para continuar.", redirect.getFlashAttributes().get("msg"));
    }
}
