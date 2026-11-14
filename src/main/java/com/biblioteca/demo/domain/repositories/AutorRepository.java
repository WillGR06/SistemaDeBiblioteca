package com.biblioteca.demo.domain.repositories;

import com.biblioteca.demo.domain.entities.AutorDomain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AutorRepository {
    AutorDomain save(AutorDomain autor);

    Optional<AutorDomain> findByAutorId(UUID idAutor);

    void deleteById(UUID idAutor);

    List<AutorDomain> findAllByAutorId(UUID idAutor);


}
