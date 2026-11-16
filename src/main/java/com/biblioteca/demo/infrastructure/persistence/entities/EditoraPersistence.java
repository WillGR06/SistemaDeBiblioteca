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
@Table(name = "Editora")
@NoArgsConstructor
@AllArgsConstructor
public class EditoraPersistence {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idEditora;

    @Column(unique = true, nullable = false)
    private String nomeEditora;
    private String paisEditora;

}
