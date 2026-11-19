package br.unisul.biblioteca.model;

/** Avaliação de uma obra exibida na página do livro. */
public record AvaliacaoResumo(String usuario, int nota, String comentario, String data) {
}
