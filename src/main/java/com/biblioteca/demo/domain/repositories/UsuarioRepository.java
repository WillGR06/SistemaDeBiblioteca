package com.biblioteca.demo.domain.repositories;

import com.biblioteca.demo.domain.entities.UsuarioDomain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {
    Optional<UsuarioDomain> findByUsuarioId(UUID idUsuario);

    UsuarioDomain save(UUID idUsuario);

    List<UsuarioDomain> findAllByUsuarioId(UUID idUsuario);

    void deleteByUsuarioId(UUID idUsuario);
}
