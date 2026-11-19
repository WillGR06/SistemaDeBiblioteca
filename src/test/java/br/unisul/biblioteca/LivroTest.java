package br.unisul.biblioteca;

import br.unisul.biblioteca.model.Livro;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Teste unitário simples (não precisa de banco). */
class LivroTest {

    @Test
    void livroGuardaOsDadosInformados() {
        Livro livro = new Livro(1, "Dom Casmurro", "Sinopse", "Português", 1899,
                "Machado de Assis", true, false, 4.5);

        assertEquals("Dom Casmurro", livro.titulo());
        assertEquals(1899, livro.anoPublicacao());
        assertTrue(livro.temEbook());
    }
}
