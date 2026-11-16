package com.biblioteca.demo.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Builder
@Entity
@Table(name = "Emprestimo")
@NoArgsConstructor
@AllArgsConstructor
public class EmprestimoPersistence {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idEmprestimo;

    @Column(unique = true, nullable = false)
    private Timestamp dataEmprestimo;
    private Timestamp dataVencimentoEmprestimo;
    private Timestamp dataBloqueioEmprestimo;
    private Timestamp dataDevolucaoEmprestimo;
    private Double valorEmprestimo;
}
