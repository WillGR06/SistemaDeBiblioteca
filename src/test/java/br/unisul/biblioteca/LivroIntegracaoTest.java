package br.unisul.biblioteca;

import br.unisul.biblioteca.model.Formato;
import br.unisul.biblioteca.model.Livro;
import br.unisul.biblioteca.repository.LivroRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LivroIntegracaoTest extends BaseIntegracaoTest {

    @Autowired
    private LivroRepository livros;

    @Test
    void buscaPorTituloEncontraOLivro() {
        List<Livro> r = livros.buscar("casm");

        assertEquals(1, r.size());
        assertEquals("Dom Casmurro", r.get(0).titulo());
        assertEquals("Machado de Assis", r.get(0).autores());
        assertTrue(r.get(0).temEbook() && r.get(0).temAudiobook());
    }

    @Test
    void buscaPorAutorEncontraOLivro() {
        assertEquals(1, livros.buscar("machado").size());
    }

    @Test
    void buscaSemResultado() {
        assertTrue(livros.buscar("livro que nao existe").isEmpty());
    }

    @Test
    void buscaVaziaListaTudo() {
        assertEquals(1, livros.buscar(null).size());
        assertEquals(1, livros.buscar("").size());
    }

    @Test
    void obraInexistenteNaoEhEncontrada() {
        assertTrue(livros.buscarPorId(-1).isEmpty());
    }

    @Test
    void mediaDeAvaliacaoUsaAFunctionDoBanco() {
        assertEquals(0.0, livros.buscarPorId(idObra).orElseThrow().media(), 0.001);

        jdbc.update("INSERT INTO avaliacao (id_usuario, id_obra, nota) VALUES (?, ?, 5)", idLeitor, idObra);
        jdbc.update("INSERT INTO avaliacao (id_usuario, id_obra, nota) VALUES (?, ?, 3)", idOutroLeitor, idObra);

        assertEquals(4.0, livros.buscarPorId(idObra).orElseThrow().media(), 0.001);
    }

    @Test
    void notaForaDoIntervaloEhRecusadaPeloBanco() {
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () ->
                jdbc.update("INSERT INTO avaliacao (id_usuario, id_obra, nota) VALUES (?, ?, 6)", idLeitor, idObra));
    }

    @Test
    void formatosDaObraListaEbookEAudiobook() {
        List<Formato> formatos = livros.formatosDaObra(idObra);

        assertEquals(2, formatos.size());
        assertTrue(formatos.stream().anyMatch(f -> f.tipo().equals("ebook") && f.id() == idEbook));
        assertTrue(formatos.stream().anyMatch(f -> f.tipo().equals("audiobook") && f.id() == idAudiobook));
    }
}
