package com.biblioteca.demo.domain.repositories;

import com.biblioteca.demo.domain.entities.EmprestimoDomain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmprestimoRepository {
    EmprestimoDomain save(EmprestimoDomain emprestimo);

    Optional<EmprestimoDomain> findByEmprestimoId(UUID id);

    List<EmprestimoDomain> findAllByEmprestimoId(UUID idEbook);

    void deleteByEmprestimoId(UUID idEmprestimo);
}
