package com.biblioteca.demo.domain.repositories;

import com.biblioteca.demo.domain.entities.ObraDomain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ObraRepository {
    ObraDomain findById(UUID id);

    Optional<ObraDomain> findByObraId(UUID id);

    List<ObraDomain> findAllByObraId(UUID id);

    void deleteByObraId(UUID id);
}
