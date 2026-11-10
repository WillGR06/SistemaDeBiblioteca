package com.biblioteca.demo.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.annotations.CurrentTimestamp;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@AllArgsConstructor
public class AvaliacaoDomain {
    private UUID idAvaliacao;
    private short notaAvaliacao;
    private String comentarioAvaliacao;
    private CurrentTimestamp dataAvaliacao;
}
