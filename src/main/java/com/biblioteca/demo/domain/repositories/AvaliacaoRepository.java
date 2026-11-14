package com.biblioteca.demo.domain.repositories;

import com.biblioteca.demo.domain.entities.AvaliacaoDomain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AvaliacaoRepository {
    AvaliacaoDomain save(AvaliacaoDomain avaliacao);

    Optional<AvaliacaoDomain> findByAvaliacaoId(UUID idAvaliacao);

    List<AvaliacaoDomain> findAllByAvaliacaoId(UUID idAvaliacao);

    void deleteById(UUID idAvaliacao);
}
