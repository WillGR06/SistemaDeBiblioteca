package br.unisul.biblioteca.model;

/**
 * Dados para a leitura de um empréstimo ativo.
 * {@code totalPaginas} é nulo quando a obra não tem número de páginas (ex.: audiobook);
 * nesse caso o progresso continua sendo mostrado em percentual.
 */
public record Acesso(int idEmprestimo, String titulo, String url, double progresso, int diasRestantes,
                     Integer totalPaginas, int paginaAtual) {

    /** Construtor para obras sem número de páginas. */
    public Acesso(int idEmprestimo, String titulo, String url, double progresso, int diasRestantes) {
        this(idEmprestimo, titulo, url, progresso, diasRestantes, null, 0);
    }

    public boolean porPagina() {
        return totalPaginas != null && totalPaginas > 0;
    }
}
