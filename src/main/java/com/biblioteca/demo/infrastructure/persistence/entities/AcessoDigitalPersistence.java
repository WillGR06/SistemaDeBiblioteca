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
@Table(name = "Acesso digital")
@NoArgsConstructor
@AllArgsConstructor
public class AcessoDigitalPersistence {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idUsuario;

    @Column(unique = true, nullable = false)
    private Timestamp dataAcesso;
    private float progressoPercentualAcesso;
}
