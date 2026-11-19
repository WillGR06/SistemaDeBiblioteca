package br.unisul.biblioteca.unit;

import br.unisul.biblioteca.model.ObraForm;
import br.unisul.biblioteca.repository.AdminRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Caminhos do AdminRepository (novo x existente, campos vazios x preenchidos), com JdbcTemplate simulado. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminRepositoryUnitTest {

    private static final String LIMPAR = "SET @id_admin = NULL";

    @Mock
    private JdbcTemplate jdbc;

    private AdminRepository repo() {
        return new AdminRepository(jdbc);
    }

    private static ObraForm form(Integer id, String autor, String ebookUrl, BigDecimal ebookValor,
                                 String audioUrl, BigDecimal audioValor) {
        return new ObraForm(id, " Título ", "  ", "Português", 2020, null, autor,
                "EPUB", ebookUrl, ebookValor, 250, " Narr ", 90, "MP3", audioUrl, audioValor);
    }

    // ---------- consultas simples ----------

    @Test
    void editorasELogVemDoBanco() {
        Map<String, Object> editora = Map.of("id_editora", 1, "nome", "Rocco");
        when(jdbc.queryForList(contains("FROM editora ORDER"))).thenReturn(List.of(editora));
        Map<String, Object> log = Map.of("acao", "editar");
        when(jdbc.queryForList(contains("log_administracao"))).thenReturn(List.of(log));

        assertEquals(List.of(editora), repo().editoras());
        assertEquals(1, repo().logRecente().size());
    }

    @Test
    void capaUrlRetornaNuloQuandoAObraNaoExisteEOEnderecoQuandoExiste() {
        when(jdbc.queryForList(contains("capa_url"), eq(String.class), eq(1))).thenReturn(List.of());
        when(jdbc.queryForList(contains("capa_url"), eq(String.class), eq(2))).thenReturn(List.of("/capas/a.png"));

        assertNull(repo().capaUrl(1));
        assertEquals("/capas/a.png", repo().capaUrl(2));
    }

    @Test
    void editoraNovaComIdNuloNoBancoRetornaZero() {
        when(jdbc.queryForList(contains("FROM editora WHERE"), eq(Integer.class), eq("Nova"))).thenReturn(List.of());
        when(jdbc.queryForObject(contains("LAST_INSERT_ID"), eq(Integer.class))).thenReturn(null);

        assertEquals(0, repo().criarEditora("Nova", "Brasil"));
        verify(jdbc).update(contains("INSERT INTO editora"), eq("Nova"), eq("Brasil"));
    }

    // ---------- carregar ----------

    @Test
    void carregarObraInexistenteRetornaVazio() {
        when(jdbc.queryForList(contains("FROM obra o"), eq(9))).thenReturn(List.of());

        assertTrue(repo().carregar(9).isEmpty());
    }

    @Test
    void carregarObraSemEbookNemAudiobookUsaValoresPadrao() {
        Map<String, Object> obra = new HashMap<>();
        obra.put("titulo", "Dom Casmurro");
        obra.put("sinopse", null);
        obra.put("idioma", "Português");
        obra.put("ano_publicacao", 1899);
        obra.put("id_editora", null);
        obra.put("autor", "Machado");
        when(jdbc.queryForList(contains("FROM obra o"), eq(1))).thenReturn(List.of(obra));
        when(jdbc.queryForList(contains("FROM ebook"), eq(1))).thenReturn(List.of());
        when(jdbc.queryForList(contains("FROM audiobook"), eq(1))).thenReturn(List.of());

        ObraForm f = repo().carregar(1).orElseThrow();

        assertEquals("Dom Casmurro", f.titulo());
        assertEquals("", f.sinopse());
        assertEquals(1899, f.anoPublicacao());
        assertNull(f.idEditora());
        assertEquals("EPUB", f.ebookFormato());
        assertEquals("MP3", f.audioFormato());
        assertNull(f.ebookValor());
        assertNull(f.audioDuracao());
    }

    @Test
    void carregarObraComEbookEAudiobookPreencheTodosOsCampos() {
        Map<String, Object> obra = new HashMap<>();
        obra.put("titulo", "Livro");
        obra.put("idioma", "Inglês");
        obra.put("id_editora", 4);
        Map<String, Object> ebook = new HashMap<>();
        ebook.put("formato", "PDF");
        ebook.put("arquivo_url", "http://e");
        ebook.put("valor_emprestimo", new BigDecimal("12.50"));
        ebook.put("numero_paginas", 300);
        Map<String, Object> audio = new HashMap<>();
        audio.put("narrador", "Fulano");
        audio.put("duracao_minutos", 120);
        audio.put("formato", "M4B");
        audio.put("arquivo_url", "http://a");
        audio.put("valor_emprestimo", 7.5);
        when(jdbc.queryForList(contains("FROM obra o"), eq(2))).thenReturn(List.of(obra));
        when(jdbc.queryForList(contains("FROM ebook"), eq(2))).thenReturn(List.of(ebook));
        when(jdbc.queryForList(contains("FROM audiobook"), eq(2))).thenReturn(List.of(audio));

        ObraForm f = repo().carregar(2).orElseThrow();

        assertEquals(4, f.idEditora());
        assertEquals("PDF", f.ebookFormato());
        assertEquals("http://e", f.ebookUrl());
        assertEquals(0, new BigDecimal("12.50").compareTo(f.ebookValor()));
        assertEquals(300, f.ebookPaginas());
        assertEquals("Fulano", f.audioNarrador());
        assertEquals(120, f.audioDuracao());
        assertEquals("M4B", f.audioFormato());
        assertEquals(0, new BigDecimal("7.5").compareTo(f.audioValor()));
    }

    // ---------- salvar ----------

    @Test
    void salvarObraNovaSemAutorEbookNemAudiobookGravaSoAObra() {
        when(jdbc.queryForObject(contains("LAST_INSERT_ID"), eq(Integer.class))).thenReturn(5);

        int id = repo().salvar(form(null, "", "", null, "", null), 1);

        assertEquals(5, id);
        verify(jdbc).update(eq("SET @id_admin = ?"), eq(1));
        verify(jdbc).update(contains("INSERT INTO obra ("), eq("Título"), isNull(), eq("Português"),
                eq(2020), isNull(), isNull());
        verify(jdbc, never()).queryForList(contains("FROM ebook"), eq(Integer.class), eq(5));
        verify(jdbc, never()).queryForList(contains("FROM audiobook"), eq(Integer.class), eq(5));
        verify(jdbc).update(LIMPAR);
    }

    @Test
    void salvarObraNovaCriaAutorEbookEAudiobookComValorZeroQuandoVazio() {
        when(jdbc.queryForObject(contains("LAST_INSERT_ID"), eq(Integer.class))).thenReturn(5);
        when(jdbc.queryForList(contains("FROM autor"), eq(Integer.class), eq("Machado"))).thenReturn(List.of());
        when(jdbc.queryForList(contains("FROM ebook"), eq(Integer.class), eq(5))).thenReturn(List.of());
        when(jdbc.queryForList(contains("FROM audiobook"), eq(Integer.class), eq(5))).thenReturn(List.of());

        int id = repo().salvar(form(null, " Machado ", " http://e ", null, " http://a ", null), 1, "/capas/c.png");

        assertEquals(5, id);
        verify(jdbc).update(contains("INSERT INTO obra ("), eq("Título"), isNull(), eq("Português"),
                eq(2020), isNull(), eq("/capas/c.png"));
        verify(jdbc).update(contains("INSERT INTO autor"), eq("Machado"));
        verify(jdbc).update(contains("DELETE FROM obra_autor"), eq(5));
        verify(jdbc).update(contains("INSERT INTO obra_autor"), eq(5), eq(5));
        verify(jdbc).update(contains("INSERT INTO ebook"), eq(5), eq("EPUB"), eq(250), eq("http://e"),
                eq(BigDecimal.ZERO));
        verify(jdbc).update(contains("INSERT INTO audiobook"), eq(5), eq("Narr"), eq(90), eq("MP3"),
                eq("http://a"), eq(BigDecimal.ZERO));
    }

    @Test
    void salvarObraExistenteReaproveitaAutorEAtualizaEbookEAudiobook() {
        BigDecimal valorEbook = new BigDecimal("9.90");
        BigDecimal valorAudio = new BigDecimal("4.50");
        when(jdbc.queryForList(contains("FROM autor"), eq(Integer.class), eq("Machado"))).thenReturn(List.of(3));
        when(jdbc.queryForList(contains("FROM ebook"), eq(Integer.class), eq(8))).thenReturn(List.of(11));
        when(jdbc.queryForList(contains("FROM audiobook"), eq(Integer.class), eq(8))).thenReturn(List.of(12));

        int id = repo().salvar(form(8, "Machado", "http://e", valorEbook, "http://a", valorAudio), 1);

        assertEquals(8, id);
        verify(jdbc).update(contains("UPDATE obra SET"), eq("Título"), isNull(), eq("Português"),
                eq(2020), isNull(), isNull(), eq(8));
        verify(jdbc, never()).update(contains("INSERT INTO autor"), eq("Machado"));
        verify(jdbc).update(contains("INSERT INTO obra_autor"), eq(8), eq(3));
        verify(jdbc).update(contains("UPDATE ebook SET"), eq("EPUB"), eq(250), eq("http://e"), eq(valorEbook), eq(11));
        verify(jdbc).update(contains("UPDATE audiobook SET"), eq("Narr"), eq(90), eq("MP3"), eq("http://a"),
                eq(valorAudio), eq(12));
    }

    @Test
    void falhaAoSalvarAindaLimpaOAdministradorDaConexao() {
        when(jdbc.queryForObject(contains("LAST_INSERT_ID"), eq(Integer.class))).thenReturn(5);
        when(jdbc.queryForList(contains("FROM autor"), eq(Integer.class), eq("Machado")))
                .thenThrow(new IllegalStateException("falha"));
        ObraForm f = form(null, "Machado", "", null, "", null);

        assertThrows(IllegalStateException.class, () -> repo().salvar(f, 1));

        verify(jdbc).update(LIMPAR);
    }

    // ---------- excluir ----------

    @Test
    void excluirApagaAObraEgravaOAdministrador() {
        repo().excluir(4, 1);

        verify(jdbc).update(eq("SET @id_admin = ?"), eq(1));
        verify(jdbc).update(contains("DELETE FROM obra"), eq(4));
        verify(jdbc).update(LIMPAR);
    }

    @Test
    void falhaAoExcluirAindaLimpaOAdministradorDaConexao() {
        when(jdbc.update(contains("DELETE FROM obra"), eq(4))).thenThrow(new IllegalStateException("fk"));
        AdminRepository repo = repo();

        assertThrows(IllegalStateException.class, () -> repo.excluir(4, 1));

        verify(jdbc).update(LIMPAR);
    }
}
