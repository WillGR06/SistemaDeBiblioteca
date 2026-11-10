package com.biblioteca.demo.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@AllArgsConstructor
public class LogAdminDomain {
    private UUID idLogAdmin;
    private UUID idAdmin;
    private String tabelaAfetadaLogAdmin;
    private UUID idRegistroLogAdmin;
    private String descricaoLogAdmin;
    private Timestamp dataAcao;


public enum AcaoAdmin{
    INSERIR,
    EDITAR,
    EXCLUIR
}

}
