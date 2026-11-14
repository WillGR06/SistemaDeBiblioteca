package com.biblioteca.demo.domain.repositories;

import com.biblioteca.demo.domain.entities.EbookDomain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EbookRepository {
    EbookDomain save(EbookDomain ebook);

    Optional<EbookDomain> findByEbookId(UUID idEbook);

    List<EbookDomain> findAllByEbookId(UUID idEbook);

    void deleteByEbookId(UUID idEbook);
}
