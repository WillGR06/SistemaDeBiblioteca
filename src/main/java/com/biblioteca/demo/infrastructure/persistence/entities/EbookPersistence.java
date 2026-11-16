package com.biblioteca.demo.infrastructure.persistence.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@Entity
@Table(name = "Ebook")
@NoArgsConstructor
@AllArgsConstructor
public class EbookPersistence {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idEbook;

    @Column(unique = true, nullable = false)
    private String ISBNEbook;
    private float tamanhoMbEbook;
    private int numeroPaginasEbook;
    private String arquivoUrlEbook;
    private Double valorEmprestimoEbook;

}
