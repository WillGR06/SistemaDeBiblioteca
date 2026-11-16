package com.biblioteca.demo.infrastructure.persistence.repositories;

import com.biblioteca.demo.domain.entities.UsuarioDomain;
import com.biblioteca.demo.infrastructure.persistence.entities.UsuarioPersistence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaUsuarioRepository extends JpaRepository<UsuarioPersistence, UUID> {
}
