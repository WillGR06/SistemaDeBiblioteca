package com.biblioteca.demo.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AudiobookDomain {
    private UUID idAudiobook;
    private String narradorAudiobook;
    private int duracaoMinutos;
    private float tamanhoMbAudiobook;
    private String arquivoUrlAudiobook;
    private Double valorEmprestimoAudiobook;

    public enum Formato{
        MP3,
        M4B,
        AAC
    }

}
