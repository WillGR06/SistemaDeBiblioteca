package com.biblioteca.demo.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@AllArgsConstructor
public class PagamentoDomain {
    private UUID idPagamento;
    private Double valorPagamento;
    private Timestamp dataPagamento;

    public enum Forma{
        CARTAO_CREDITO,
        CARTAO_DEBITO,
        PIX,
        BOLETO
    }

    public enum Status{
        PENDENTE,
        APROVADO,
        RECUSADO,
        ESTORNADO
    }
}
