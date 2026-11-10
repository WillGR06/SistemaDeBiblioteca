package com.biblioteca.demo.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class EditoraDomain {
    private UUID idEditora;
    private String nomeEditora;
    private String paisEditora;

}
