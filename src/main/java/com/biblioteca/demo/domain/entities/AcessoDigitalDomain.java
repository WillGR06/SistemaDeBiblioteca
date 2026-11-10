package com.biblioteca.demo.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@AllArgsConstructor
public class AcessoDigitalDomain {
    private UUID idAcesso;
    private Timestamp dataAcesso;
    private float progressoPercentualAcesso;

}
