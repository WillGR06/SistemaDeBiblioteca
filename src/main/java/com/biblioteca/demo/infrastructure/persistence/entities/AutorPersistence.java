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
@Table(name = "Autor")
@NoArgsConstructor
@AllArgsConstructor
public class AutorPersistence {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idAutor;
    @Column(unique = true, nullable = false)
    private String nomeAutor;
    private String biografiaAutor;
    private String nacionalidade;
}
