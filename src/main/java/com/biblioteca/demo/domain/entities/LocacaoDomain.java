package com.biblioteca.demo.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class LocacaoDomain {
    private UUID idLocacao;
    private UUID idUsuario;
    private UUID idLivro;
    private BigDecimal valorLocacao;
    private LocalDateTime dataInicioLocacao;
    private LocalDateTime dataFimLocacao;

}
