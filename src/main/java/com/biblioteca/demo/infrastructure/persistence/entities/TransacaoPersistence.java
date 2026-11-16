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
@Table(name = "Transacao")
@NoArgsConstructor
@AllArgsConstructor
public class TransacaoPersistence {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idGenero;

    @Column(unique = true, nullable = false)
    private String nomeGenero;
}
