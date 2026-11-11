package com.biblioteca.demo.domain.repositories;

import com.biblioteca.demo.domain.entities.AcessoDigitalDomain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AcessoDigitalRepository {
    AcessoDigitalDomain save(AcessoDigitalDomain acessoDigital);

    Optional<AcessoDigitalDomain> findById(UUID idAcessoDigital);

    void deleteById(UUID idAcessoDigital);

    List<AcessoDigitalDomain> findAllByAcessoDigitalId(UUID idAcessoDigital);
}
