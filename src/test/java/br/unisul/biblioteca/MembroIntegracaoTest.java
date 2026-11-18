package br.unisul.biblioteca;

import br.unisul.biblioteca.model.Membro;
import br.unisul.biblioteca.repository.MembroRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MembroIntegracaoTest extends BaseIntegracaoTest {

    @Autowired
    private MembroRepository membros;

    @Test
    void listaTodosOsMembros() {
        List<Membro> lista = membros.listar(null);

        assertEquals(3, lista.size());
    }

    @Test
    void buscaPorNomeOuEmail() {
        assertEquals(1, membros.listar("outro@").size());
        assertEquals(1, membros.listar("Admin Teste").size());
        assertTrue(membros.listar("ninguem").isEmpty());
    }

    @Test
    void desativarEAtivarMembroGravaHistorico() {
        membros.alterarStatus(idLeitor, false, idAdmin);
        assertEquals(0, contar("SELECT ativo FROM usuario WHERE id_usuario = ?", idLeitor));

        membros.alterarStatus(idLeitor, true, idAdmin);
        assertEquals(1, contar("SELECT ativo FROM usuario WHERE id_usuario = ?", idLeitor));

        assertEquals(2, contar("""
                SELECT COUNT(*) FROM log_administracao
                 WHERE tabela_afetada = 'usuario' AND id_registro = ? AND id_administrador = ?
                """, idLeitor, idAdmin));
    }

    @Test
    void administradorNaoDesativaASiMesmo() {
        assertThrows(IllegalArgumentException.class, () -> membros.alterarStatus(idAdmin, false, idAdmin));
        assertEquals(1, contar("SELECT ativo FROM usuario WHERE id_usuario = ?", idAdmin));
    }

    @Test
    void promoverERebaixarMembro() {
        membros.alterarPerfil(idLeitor, "administrador", idAdmin);
        assertEquals(1, contar("SELECT COUNT(*) FROM usuario WHERE id_usuario = ? AND perfil = 'administrador'", idLeitor));

        membros.alterarPerfil(idLeitor, "usuario", idAdmin);
        assertEquals(1, contar("SELECT COUNT(*) FROM usuario WHERE id_usuario = ? AND perfil = 'usuario'", idLeitor));
    }

    @Test
    void administradorNaoAlteraOProprioPerfil() {
        assertThrows(IllegalArgumentException.class, () -> membros.alterarPerfil(idAdmin, "usuario", idAdmin));
    }

    @Test
    void perfilInvalidoEhRecusado() {
        assertThrows(IllegalArgumentException.class, () -> membros.alterarPerfil(idLeitor, "superusuario", idAdmin));
    }

    @Test
    void membroInexistenteEhRecusado() {
        assertThrows(IllegalArgumentException.class, () -> membros.alterarStatus(-1, false, idAdmin));
    }
}
