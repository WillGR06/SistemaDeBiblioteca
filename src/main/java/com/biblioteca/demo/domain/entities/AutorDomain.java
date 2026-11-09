package com.biblioteca.demo.domain.entities;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AutorDomain {
    private UUID idAutor;
    private String nomeAutor;
    private String biografiaAutor;
    private String nacionalidade;
}
