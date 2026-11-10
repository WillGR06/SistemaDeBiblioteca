package com.biblioteca.demo.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class EbookDomain {
    private UUID idEbook;
    private String ISBNEbook;
    private float tamanhoMbEbook;
    private int numeroPaginasEbook;
    private String arquivoUrlEbook;
    private Double valorEmprestimoEbook;

    public enum Ebook {
        PDF,
        EPUB,
        MOBI
    }
}
