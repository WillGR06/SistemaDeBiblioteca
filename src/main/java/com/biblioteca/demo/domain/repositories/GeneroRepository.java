package com.biblioteca.demo.domain.repositories;

import com.biblioteca.demo.domain.entities.GeneroDomain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GeneroRepository {
    GeneroDomain findById(UUID id);

    Optional<GeneroDomain> findByGeneroId(UUID id);

    List<GeneroDomain> findAllByGeneroId(UUID id);

    void deleteByGeneroId(UUID id);
}
