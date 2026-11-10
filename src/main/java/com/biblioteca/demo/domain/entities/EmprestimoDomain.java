package com.biblioteca.demo.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@AllArgsConstructor
public class EmprestimoDomain {
    private UUID emprestimoId;
    private Timestamp dataEmprestimo;
    private Timestamp dataVencimentoEmprestimo;
    private Timestamp dataBloqueioEmprestimo;
    private Timestamp dataDevolucaoEmprestimo;
    private Double valorEmprestimo;

    public enum status{
        ATIVO,
        DEVOLVIDO,
        EXPIRADO
    }


}
