package br.unisul.biblioteca;

import br.unisul.biblioteca.model.Usuario;
import br.unisul.biblioteca.security.UsuarioLogado;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Testa as telas, o login, as permissões e o fluxo completo de empréstimo e devolução. */
class WebIntegracaoTest extends BaseIntegracaoTest {

    @Autowired
    private MockMvc mvc;

    private UsuarioLogado leitor() {
        return new UsuarioLogado(new Usuario(idLeitor, "Leitor Teste", "leitor@teste.com", "x", "usuario", true));
    }

    private UsuarioLogado administrador() {
        return new UsuarioLogado(new Usuario(idAdmin, "Admin Teste", "admin@teste.com", "x", "administrador", true));
    }

    private String corpo(ResultActions r) throws Exception {
        return r.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    void catalogoEPublicoEMostraOsLivros() throws Exception {
        ResultActions r = mvc.perform(get("/")).andExpect(status().isOk());

        assertTrue(corpo(r).contains("Dom Casmurro"));
    }

    @Test
    void paginaDoLivroEPublica() throws Exception {
        ResultActions r = mvc.perform(get("/livros/" + idObra)).andExpect(status().isOk());

        assertTrue(corpo(r).contains("Dom Casmurro"));
    }

    @Test
    void livroInexistenteRetorna404() throws Exception {
        mvc.perform(get("/livros/-1")).andExpect(status().isNotFound());
    }

    @Test
    void meusEmprestimosExigeLogin() throws Exception {
        mvc.perform(get("/emprestimos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void loginComSenhaCorretaAutentica() throws Exception {
        mvc.perform(formLogin("/login").user("email", "leitor@teste.com").password("password", SENHA_PADRAO))
                .andExpect(authenticated())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void loginComSenhaErradaEhRecusado() throws Exception {
        mvc.perform(formLogin("/login").user("email", "leitor@teste.com").password("password", "errada"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void leitorNaoAcessaAreaDoAdministrador() throws Exception {
        mvc.perform(get("/admin/obras").with(user(leitor())))
                .andExpect(status().isForbidden());
    }

    @Test
    void visitanteNaoAcessaAreaDoAdministrador() throws Exception {
        mvc.perform(get("/admin/obras"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void administradorAcessaAreaDoAdministrador() throws Exception {
        ResultActions r = mvc.perform(get("/admin/obras").with(user(administrador())))
                .andExpect(status().isOk());

        assertTrue(corpo(r).contains("Dom Casmurro"));
    }

    @Test
    void cadastroCriaUsuarioEImpedeEmailRepetido() throws Exception {
        mvc.perform(post("/cadastro").with(csrf())
                        .param("nome", "Fulano").param("email", "fulano@teste.com")
                        .param("senha", "abc123").param("confirmar", "abc123"))
                .andExpect(redirectedUrl("/login"));
        assertEquals(1, contar("SELECT COUNT(*) FROM usuario WHERE email = 'fulano@teste.com'"));

        ResultActions repetido = mvc.perform(post("/cadastro").with(csrf())
                        .param("nome", "Fulano 2").param("email", "fulano@teste.com")
                        .param("senha", "abc123").param("confirmar", "abc123"))
                .andExpect(status().isOk());
        assertTrue(corpo(repetido).contains("cadastrado"));
        assertEquals(1, contar("SELECT COUNT(*) FROM usuario WHERE email = 'fulano@teste.com'"));
    }

    @Test
    void cadastroComSenhasDiferentesNaoCriaUsuario() throws Exception {
        mvc.perform(post("/cadastro").with(csrf())
                        .param("nome", "Beltrano").param("email", "beltrano@teste.com")
                        .param("senha", "abc123").param("confirmar", "outra123"))
                .andExpect(status().isOk());

        assertEquals(0, contar("SELECT COUNT(*) FROM usuario WHERE email = 'beltrano@teste.com'"));
    }

    @Test
    void fluxoCompletoEmprestarLerEDevolver() throws Exception {
        // 1. empresta o e-book
        mvc.perform(post("/emprestimos").with(user(leitor())).with(csrf())
                        .param("idEbook", String.valueOf(idEbook)).param("forma", "pix"))
                .andExpect(redirectedUrl("/emprestimos"));

        int idEmprestimo = jdbc.queryForObject(
                "SELECT id_emprestimo FROM emprestimo WHERE id_usuario = ?", Integer.class, idLeitor);

        // 2. aparece em "Meus empréstimos" como Emprestado
        ResultActions lista = mvc.perform(get("/emprestimos").with(user(leitor()))).andExpect(status().isOk());
        assertTrue(corpo(lista).contains("Emprestado"));

        // 3. a página de leitura abre
        ResultActions leitura = mvc.perform(get("/emprestimos/" + idEmprestimo + "/ler").with(user(leitor())))
                .andExpect(status().isOk());
        assertTrue(corpo(leitura).contains("Dom Casmurro"));

        // 4. devolve
        mvc.perform(post("/emprestimos/" + idEmprestimo + "/devolver").with(user(leitor())).with(csrf()))
                .andExpect(redirectedUrl("/emprestimos"));

        // 5. aparece como Devolvido e a leitura é bloqueada
        ResultActions depois = mvc.perform(get("/emprestimos").with(user(leitor()))).andExpect(status().isOk());
        assertTrue(corpo(depois).contains("Devolvido"));
        mvc.perform(get("/emprestimos/" + idEmprestimo + "/ler").with(user(leitor())))
                .andExpect(redirectedUrl("/emprestimos"));
    }

    @Test
    void usuarioNaoLeEmprestimoDeOutraPessoa() throws Exception {
        int id = jdbcEmprestimoDoLeitor();

        UsuarioLogado outro = new UsuarioLogado(
                new Usuario(idOutroLeitor, "Outro Leitor", "outro@teste.com", "x", "usuario", true));
        mvc.perform(get("/emprestimos/" + id + "/ler").with(user(outro)))
                .andExpect(redirectedUrl("/emprestimos"));
    }

    private int jdbcEmprestimoDoLeitor() throws Exception {
        mvc.perform(post("/emprestimos").with(user(leitor())).with(csrf())
                .param("idEbook", String.valueOf(idEbook)).param("forma", "pix"));
        return jdbc.queryForObject("SELECT id_emprestimo FROM emprestimo WHERE id_usuario = ?", Integer.class, idLeitor);
    }

    @Test
    void postSemTokenCsrfEhBloqueado() throws Exception {
        mvc.perform(post("/emprestimos").with(user(leitor()))
                        .param("idEbook", String.valueOf(idEbook)).param("forma", "pix"))
                .andExpect(status().isForbidden());
    }
}
