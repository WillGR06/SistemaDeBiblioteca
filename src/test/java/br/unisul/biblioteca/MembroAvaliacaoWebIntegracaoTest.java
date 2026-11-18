package br.unisul.biblioteca;

import br.unisul.biblioteca.model.Usuario;
import br.unisul.biblioteca.repository.AvaliacaoRepository;
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
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Telas de avaliação (leitor) e de gestão de membros (administrador). */
class MembroAvaliacaoWebIntegracaoTest extends BaseIntegracaoTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AvaliacaoRepository avaliacoes;

    private UsuarioLogado leitor() {
        return new UsuarioLogado(new Usuario(idLeitor, "Leitor Teste", "leitor@teste.com", "x", "usuario", true));
    }

    private UsuarioLogado administrador() {
        return new UsuarioLogado(new Usuario(idAdmin, "Admin Teste", "admin@teste.com", "x", "administrador", true));
    }

    private String corpo(ResultActions r) throws Exception {
        return r.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    // ---------- avaliação ----------

    @Test
    void avaliarExigeLogin() throws Exception {
        mvc.perform(post("/livros/" + idObra + "/avaliar").with(csrf()).param("nota", "5"))
                .andExpect(status().is3xxRedirection());

        assertEquals(0, contar("SELECT COUNT(*) FROM avaliacao"));
    }

    @Test
    void leitorAvaliaOLivro() throws Exception {
        mvc.perform(post("/livros/" + idObra + "/avaliar").with(user(leitor())).with(csrf())
                        .param("nota", "4").param("comentario", "Excelente livro"))
                .andExpect(redirectedUrl("/livros/" + idObra));

        assertEquals(1, contar("SELECT COUNT(*) FROM avaliacao WHERE id_usuario = ? AND nota = 4", idLeitor));
    }

    @Test
    void notaInvalidaNaoGravaNada() throws Exception {
        mvc.perform(post("/livros/" + idObra + "/avaliar").with(user(leitor())).with(csrf())
                        .param("nota", "9"))
                .andExpect(redirectedUrl("/livros/" + idObra));

        assertEquals(0, contar("SELECT COUNT(*) FROM avaliacao"));
    }

    @Test
    void avaliarLivroInexistenteRetorna404() throws Exception {
        mvc.perform(post("/livros/-1/avaliar").with(user(leitor())).with(csrf()).param("nota", "5"))
                .andExpect(status().isNotFound());
    }

    @Test
    void paginaDoLivroMostraAsAvaliacoes() throws Exception {
        avaliacoes.salvar(idOutroLeitor, idObra, 5, "Excelente livro");

        ResultActions r = mvc.perform(get("/livros/" + idObra)).andExpect(status().isOk());

        String html = corpo(r);
        assertTrue(html.contains("Excelente livro"));
        assertTrue(html.contains("Outro Leitor"));
    }

    @Test
    void postSemTokenCsrfEhBloqueado() throws Exception {
        mvc.perform(post("/livros/" + idObra + "/avaliar").with(user(leitor())).param("nota", "5"))
                .andExpect(status().isForbidden());
    }

    // ---------- gestão de membros ----------

    @Test
    void leitorNaoAcessaGestaoDeMembros() throws Exception {
        mvc.perform(get("/admin/membros").with(user(leitor()))).andExpect(status().isForbidden());
    }

    @Test
    void visitanteNaoAcessaGestaoDeMembros() throws Exception {
        mvc.perform(get("/admin/membros")).andExpect(status().is3xxRedirection());
    }

    @Test
    void administradorVeAListaDeMembros() throws Exception {
        ResultActions r = mvc.perform(get("/admin/membros").with(user(administrador())))
                .andExpect(status().isOk());

        assertTrue(corpo(r).contains("leitor@teste.com"));
    }

    @Test
    void leitorNaoPodeDesativarMembros() throws Exception {
        mvc.perform(post("/admin/membros/" + idOutroLeitor + "/status").with(user(leitor())).with(csrf())
                        .param("ativo", "false"))
                .andExpect(status().isForbidden());

        assertEquals(1, contar("SELECT ativo FROM usuario WHERE id_usuario = ?", idOutroLeitor));
    }

    @Test
    void membroDesativadoNaoConsegueEntrar() throws Exception {
        mvc.perform(post("/admin/membros/" + idLeitor + "/status").with(user(administrador())).with(csrf())
                        .param("ativo", "false"))
                .andExpect(redirectedUrl("/admin/membros"));
        assertEquals(0, contar("SELECT ativo FROM usuario WHERE id_usuario = ?", idLeitor));

        mvc.perform(formLogin("/login").user("email", "leitor@teste.com").password("password", SENHA_PADRAO))
                .andExpect(unauthenticated());
    }

    @Test
    void administradorNaoDesativaASiMesmoPelaTela() throws Exception {
        mvc.perform(post("/admin/membros/" + idAdmin + "/status").with(user(administrador())).with(csrf())
                        .param("ativo", "false"))
                .andExpect(redirectedUrl("/admin/membros"));

        assertEquals(1, contar("SELECT ativo FROM usuario WHERE id_usuario = ?", idAdmin));
    }

    @Test
    void administradorPromoveUmLeitor() throws Exception {
        mvc.perform(post("/admin/membros/" + idLeitor + "/perfil").with(user(administrador())).with(csrf())
                        .param("perfil", "administrador"))
                .andExpect(redirectedUrl("/admin/membros"));

        assertEquals(1, contar("SELECT COUNT(*) FROM usuario WHERE id_usuario = ? AND perfil = 'administrador'", idLeitor));
    }
}
