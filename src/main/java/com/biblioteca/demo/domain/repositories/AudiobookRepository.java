package com.biblioteca.demo.domain.repositories;


import com.biblioteca.demo.domain.entities.AudiobookDomain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AudiobookRepository {
    AudiobookDomain save(AudiobookDomain audiobookDomain);

    void deleteById(UUID idAudiobook);

    List<AudiobookDomain> findAllByAudiobookId(UUID idAudiobook);

    Optional<AudiobookDomain> findById(UUID idAudiobook);
}
