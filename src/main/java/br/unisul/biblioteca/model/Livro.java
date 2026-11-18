package br.unisul.biblioteca.model;

/** Resumo de uma obra para exibir no catálogo. */
public record Livro(
        int idObra,
        String titulo,
        String sinopse,
        String idioma,
        Integer anoPublicacao,
        String autores,
        boolean temEbook,
        boolean temAudiobook,
        double media,
        String capaUrl) {

    /** Obra sem imagem de capa. */
    public Livro(int idObra, String titulo, String sinopse, String idioma, Integer anoPublicacao,
                 String autores, boolean temEbook, boolean temAudiobook, double media) {
        this(idObra, titulo, sinopse, idioma, anoPublicacao, autores, temEbook, temAudiobook, media, null);
    }

    public boolean temCapa() {
        return capaUrl != null && !capaUrl.isBlank();
    }
}
