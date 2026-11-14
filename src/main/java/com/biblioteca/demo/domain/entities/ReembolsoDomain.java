package com.biblioteca.demo.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ReembolsoDomain {
    private UUID idReembolso;
    private PagamentoDomain pagamentoDomain;
    private String gatewayRefundId;
    private BigDecimal valorReembolso;
    private String motivoReembolso;
    private LocalDateTime dataSolicitacao;
    private LocalDateTime dataConclusao;
}
