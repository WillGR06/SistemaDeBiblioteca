package br.unisul.biblioteca.model;

import java.math.BigDecimal;

/** Dados do formulário de cadastro/edição de obra (área do administrador). */
public record ObraForm(
        Integer idObra,
        String titulo,
        String sinopse,
        String idioma,
        Integer anoPublicacao,
        Integer idEditora,
        String autor,
        String ebookFormato,
        String ebookUrl,
        BigDecimal ebookValor,
        Integer ebookPaginas,
        String audioNarrador,
        Integer audioDuracao,
        String audioFormato,
        String audioUrl,
        BigDecimal audioValor) {

    public static ObraForm vazio() {
        return new ObraForm(null, "", "", "Português", null, null, "",
                "EPUB", "", null, null, "", null, "MP3", "", null);
    }
}
