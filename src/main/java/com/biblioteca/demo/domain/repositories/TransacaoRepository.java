package com.biblioteca.demo.domain.repositories;

import com.biblioteca.demo.domain.entities.TransacaoDomain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransacaoRepository {
    TransacaoDomain save(TransacaoDomain transacao);

    Optional<TransacaoDomain> findByTransacaoId(UUID idEbook);

    void deleteByTransacaoId(UUID idEbook);

    List<TransacaoDomain> findAllByTransacaoId(UUID transacaoId);
}
