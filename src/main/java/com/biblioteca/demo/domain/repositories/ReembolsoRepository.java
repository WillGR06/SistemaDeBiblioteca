package com.biblioteca.demo.domain.repositories;

import com.biblioteca.demo.domain.entities.ReembolsoDomain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReembolsoRepository {
    ReembolsoDomain save(UUID idReembolso);

    List<ReembolsoDomain> findAllByReembolsoId(UUID idReembolso);

    Optional<ReembolsoDomain> findByReembolsoId(UUID idReembolso);

    void deleteByReembolsoId(UUID idReembolso);
}
