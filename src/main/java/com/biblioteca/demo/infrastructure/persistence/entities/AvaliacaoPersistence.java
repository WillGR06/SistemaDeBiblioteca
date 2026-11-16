package com.biblioteca.demo.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CurrentTimestamp;

import java.util.UUID;

@Data
@Builder
@Entity
@Table(name = "Avaliacao")
@NoArgsConstructor
@AllArgsConstructor
public class AvaliacaoPersistence {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idAvaliacao;

    @Column(unique = true, nullable = false)
    private short notaAvaliacao;
    private String comentarioAvaliacao;
    private CurrentTimestamp dataAvaliacao;

}
