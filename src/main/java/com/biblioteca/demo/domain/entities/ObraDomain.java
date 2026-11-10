package com.biblioteca.demo.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;
import java.time.Year;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ObraDomain {
    private UUID idObra;
    private String tituloObra;
    private String sinopseObra;
    private String idiomaObra;
    private Year anoObra;
    private String capaUrlObra;
    private Timestamp dataCadastroObra;
}
