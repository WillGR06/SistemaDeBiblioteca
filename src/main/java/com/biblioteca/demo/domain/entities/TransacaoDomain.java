package com.biblioteca.demo.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class TransacaoDomain {
    private UUID idTransacao;
    private String gatewayIdTransacao;
    private String chaveIdempotenciaTransacao;
    private PagamentoDomain.Forma metodoPagamento;
    private PagamentoDomain.Status status;
    private LocalDateTime dataCriacao;

}
