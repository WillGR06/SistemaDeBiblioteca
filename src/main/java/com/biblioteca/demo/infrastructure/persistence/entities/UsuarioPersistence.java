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
@Table(name = "Usuario")
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioPersistence {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idUsuario;
    private String nomeUsuario;
    private String emailUsuario;
    private String senhaUsuario;
    private Timestamp dataCadastroUsuario;
    private boolean ativoUsuario;


}
