package br.unisul.biblioteca;

import br.unisul.biblioteca.model.AvaliacaoResumo;
import br.unisul.biblioteca.repository.AvaliacaoRepository;
import br.unisul.biblioteca.repository.LivroRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvaliacaoIntegracaoTest extends BaseIntegracaoTest {

    @Autowired
    private AvaliacaoRepository avaliacoes;

    @Autowired
    private LivroRepository livros;

    @Test
    void avaliarCriaRegistroEAtualizaAMedia() {
        avaliacoes.salvar(idLeitor, idObra, 5, "Excelente livro");
        avaliacoes.salvar(idOutroLeitor, idObra, 3, null);

        assertEquals(2, contar("SELECT COUNT(*) FROM avaliacao WHERE id_obra = ?", idObra));
        assertEquals(4.0, livros.buscarPorId(idObra).orElseThrow().media(), 0.001);
    }

    @Test
    void avaliarDeNovoAtualizaSemDuplicar() {
        avaliacoes.salvar(idLeitor, idObra, 5, "Primeira opinião");
        avaliacoes.salvar(idLeitor, idObra, 2, "Mudei de ideia");

        assertEquals(1, contar("SELECT COUNT(*) FROM avaliacao WHERE id_usuario = ? AND id_obra = ?",
                idLeitor, idObra));
        AvaliacaoResumo minha = avaliacoes.doUsuario(idLeitor, idObra).orElseThrow();
        assertEquals(2, minha.nota());
        assertEquals("Mudei de ideia", minha.comentario());
    }

    @Test
    void notaForaDoIntervaloEhRecusada() {
        assertThrows(IllegalArgumentException.class, () -> avaliacoes.salvar(idLeitor, idObra, 0, "x"));
        assertThrows(IllegalArgumentException.class, () -> avaliacoes.salvar(idLeitor, idObra, 6, "x"));
        assertEquals(0, contar("SELECT COUNT(*) FROM avaliacao"));
    }

    @Test
    void comentarioEmBrancoViraNulo() {
        avaliacoes.salvar(idLeitor, idObra, 4, "   ");

        assertNull(avaliacoes.doUsuario(idLeitor, idObra).orElseThrow().comentario());
    }

    @Test
    void comentarioMuitoLongoEhRecusado() {
        String longo = "a".repeat(1001);

        assertThrows(IllegalArgumentException.class, () -> avaliacoes.salvar(idLeitor, idObra, 4, longo));
    }

    @Test
    void listaTrazNomeENotaDeQuemAvaliou() {
        avaliacoes.salvar(idLeitor, idObra, 5, "Muito bom");

        List<AvaliacaoResumo> lista = avaliacoes.listarPorObra(idObra);

        assertEquals(1, lista.size());
        assertEquals("Leitor Teste", lista.get(0).usuario());
        assertTrue(lista.get(0).nota() == 5);
    }

    @Test
    void obraInexistenteNaoAceitaAvaliacao() {
        assertThrows(RuntimeException.class, () -> avaliacoes.salvar(idLeitor, -1, 5, "x"));
    }
}
