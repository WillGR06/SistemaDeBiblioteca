package com.biblioteca.demo.domain.repositories;

import com.biblioteca.demo.domain.entities.LogAdminDomain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LogAdminRepository {
    LogAdminDomain findById(UUID id);

    Optional<LogAdminDomain> findByLogAdminId(UUID id);

    List<LogAdminDomain> findAllByLogAdminId(UUID id);

    void deleteByLogAdminId(UUID id);
}
