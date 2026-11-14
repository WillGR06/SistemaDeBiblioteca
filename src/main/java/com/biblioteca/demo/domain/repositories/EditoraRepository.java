package com.biblioteca.demo.domain.repositories;

import com.biblioteca.demo.domain.entities.EditoraDomain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EditoraRepository {
    EditoraDomain save(EditoraDomain editoraDomain);

    Optional<EditoraDomain> findByEditoraId(UUID idEditora);

    List<EditoraDomain> findAllByEditoraId(UUID idEditora);

    void deleteByEditoraId(UUID idEditora);
}
