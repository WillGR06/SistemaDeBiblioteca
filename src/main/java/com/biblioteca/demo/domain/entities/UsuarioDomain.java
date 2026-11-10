package com.biblioteca.demo.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@AllArgsConstructor
public class UsuarioDomain {
    private UUID idUsuario;
    private String nomeUsuario;
    private String emailUsuario;
    private String senhaHashUsuario;
    private Timestamp dataCadastroUsuario;
    private boolean ativoUsuario;

    public enum Usuario{
        USUARIO,
        FUNCIONARIO,
        ADMINISTRADOR
    }
}

