package br.unisul.biblioteca;

import br.unisul.biblioteca.model.Acesso;
import br.unisul.biblioteca.model.Livro;
import br.unisul.biblioteca.util.Erros;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Cobre os caminhos alternativos (se/senão) dos models e da conversão de erros. */
class ModelosErrosRamosTest {

    private static final String GENERICA = "Não foi possível concluir a operação. Tente novamente.";

    // ---------- Livro ----------

    @Test
    void livroSemCapaOuComCapaEmBrancoNaoTemCapa() {
        Livro semCapa = new Livro(1, "T", "S", "Português", 2000, "A", true, false, 0);
        Livro emBranco = new Livro(1, "T", "S", "Português", 2000, "A", true, false, 0, "   ");

        assertFalse(semCapa.temCapa());
        assertNull(semCapa.capaUrl());
        assertFalse(emBranco.temCapa());
    }

    @Test
    void livroComCapaTemCapa() {
        Livro livro = new Livro(1, "T", "S", "Português", 2000, "A", true, false, 0, "/capas/a.png");

        assertTrue(livro.temCapa());
    }

    // ---------- Acesso ----------

    @Test
    void acessoSemTotalDePaginasUsaPercentual() {
        assertFalse(new Acesso(1, "T", "u", 10, 5).porPagina());
        assertFalse(new Acesso(1, "T", "u", 10, 5, 0, 0).porPagina());
    }

    @Test
    void acessoComTotalDePaginasUsaPaginas() {
        Acesso acesso = new Acesso(1, "T", "u", 40, 5, 250, 100);

        assertTrue(acesso.porPagina());
        assertEquals(100, acesso.paginaAtual());
    }

    // ---------- Erros ----------

    @Test
    void erroDeIntegridadeViraMensagemDeRegistroEmUso() {
        assertEquals("Operação não permitida: o registro está em uso ou os dados são inválidos.",
                Erros.mensagem(new DataIntegrityViolationException("fk")));
    }

    @Test
    void erroSqlComOutroCodigoViraMensagemGenerica() {
        assertEquals(GENERICA, Erros.mensagem(new RuntimeException(new SQLException("x", "23000"))));
    }

    @Test
    void argumentoInvalidoDentroDaCausaTambemEhRepassado() {
        RuntimeException envolvida = new RuntimeException("topo", new IllegalArgumentException("Dentro"));

        assertEquals("Dentro", Erros.mensagem(envolvida));
    }

    @Test
    void erroSqlDeProcedureNaPontaDaCadeiaEhRepassado() {
        SQLException sql = new SQLException("Limite atingido", "45000");
        Exception cadeia = new RuntimeException("a", new IllegalStateException("b", sql));

        assertEquals("Limite atingido", Erros.mensagem(cadeia));
    }
}
