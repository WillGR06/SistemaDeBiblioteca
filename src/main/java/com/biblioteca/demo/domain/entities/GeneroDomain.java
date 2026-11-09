package com.biblioteca.demo.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class GeneroDomain {
    private UUID idGenero;
    private String nomeGenero;
}
