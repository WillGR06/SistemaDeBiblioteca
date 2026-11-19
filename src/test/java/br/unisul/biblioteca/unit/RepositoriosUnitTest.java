package br.unisul.biblioteca.unit;

import br.unisul.biblioteca.repository.AdminRepository;
import br.unisul.biblioteca.repository.AvaliacaoRepository;
import br.unisul.biblioteca.repository.EmprestimoRepository;
import br.unisul.biblioteca.repository.MembroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Regras de validação dos repositórios, com o JdbcTemplate simulado (sem banco). */
@ExtendWith(MockitoExtension.class)
class RepositoriosUnitTest {

    @Mock
    private JdbcTemplate jdbc;

    // ---------- AvaliacaoRepository ----------

    @Test
    void notaAbaixoDeUmEhRecusada() {
        AvaliacaoRepository repo = new AvaliacaoRepository(jdbc);

        assertThrows(IllegalArgumentException.class, () -> repo.salvar(1, 2, 0, "x"));
        verifyNoInteractions(jdbc);
    }

    @Test
    void notaAcimaDeCincoEhRecusada() {
        AvaliacaoRepository repo = new AvaliacaoRepository(jdbc);

        assertThrows(IllegalArgumentException.class, () -> repo.salvar(1, 2, 6, "x"));
        verifyNoInteractions(jdbc);
    }

    @Test
    void comentarioComMaisDeMilCaracteresEhRecusado() {
        AvaliacaoRepository repo = new AvaliacaoRepository(jdbc);
        String longo = "a".repeat(1001);

        assertThrows(IllegalArgumentException.class, () -> repo.salvar(1, 2, 5, longo));
        verifyNoInteractions(jdbc);
    }

    @Test
    void avaliacaoValidaRemoveEspacosDoComentario() {
        new AvaliacaoRepository(jdbc).salvar(1, 2, 5, "  Ótimo  ");

        verify(jdbc).update(anyString(), eq(1), eq(2), eq(5), eq("Ótimo"));
    }

    @Test
    void comentarioEmBrancoEhGravadoComoNulo() {
        new AvaliacaoRepository(jdbc).salvar(1, 2, 4, "   ");

        verify(jdbc).update(anyString(), eq(1), eq(2), eq(4), isNull());
    }

    // ---------- MembroRepository ----------

    @Test
    void administradorNaoDesativaASiMesmo() {
        MembroRepository repo = new MembroRepository(jdbc);

        assertThrows(IllegalArgumentException.class, () -> repo.alterarStatus(1, false, 1));
        verifyNoInteractions(jdbc);
    }

    @Test
    void administradorNaoAlteraOProprioPerfil() {
        MembroRepository repo = new MembroRepository(jdbc);

        assertThrows(IllegalArgumentException.class, () -> repo.alterarPerfil(1, "usuario", 1));
        verifyNoInteractions(jdbc);
    }

    @Test
    void perfilInvalidoEhRecusado() {
        MembroRepository repo = new MembroRepository(jdbc);

        assertThrows(IllegalArgumentException.class, () -> repo.alterarPerfil(2, "superusuario", 1));
        verifyNoInteractions(jdbc);
    }

    @Test
    void membroInexistenteEhRecusado() {
        when(jdbc.queryForList(anyString(), eq(String.class), eq(9))).thenReturn(List.of());
        MembroRepository repo = new MembroRepository(jdbc);

        assertThrows(IllegalArgumentException.class, () -> repo.alterarStatus(9, false, 1));
    }

    @Test
    void desativarMembroAtualizaEGravaNoHistorico() {
        when(jdbc.queryForList(anyString(), eq(String.class), eq(2))).thenReturn(List.of("Fulano"));

        new MembroRepository(jdbc).alterarStatus(2, false, 1);

        verify(jdbc).update(anyString(), eq(false), eq(2));
        verify(jdbc).update(anyString(), eq(1), eq(2), eq("Membro desativado: Fulano"));
    }

    @Test
    void promoverMembroAtualizaEGravaNoHistorico() {
        when(jdbc.queryForList(anyString(), eq(String.class), eq(2))).thenReturn(List.of("Fulano"));

        new MembroRepository(jdbc).alterarPerfil(2, "administrador", 1);

        verify(jdbc).update(anyString(), eq("administrador"), eq(2));
        verify(jdbc).update(anyString(), eq(1), eq(2), eq("Perfil de Fulano alterado para administrador"));
    }

    // ---------- EmprestimoRepository ----------

    @Test
    void naoDevolveEmprestimoDeOutraPessoa() {
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(1), eq(2))).thenReturn(0);
        EmprestimoRepository repo = new EmprestimoRepository(jdbc);

        assertThrows(IllegalArgumentException.class, () -> repo.devolver(1, 2));
    }

    @Test
    void progressoAcimaDeCemFicaEmCem() {
        new EmprestimoRepository(jdbc).salvarProgresso(1, 2, 150);

        verify(jdbc).update(anyString(), eq(100.0), eq(1), eq(2));
    }

    @Test
    void progressoNegativoFicaEmZero() {
        new EmprestimoRepository(jdbc).salvarProgresso(1, 2, -20);

        verify(jdbc).update(anyString(), eq(0.0), eq(1), eq(2));
    }

    @Test
    void progressoPorPaginaEnviaPaginaEIdsParaOBanco() {
        new EmprestimoRepository(jdbc).salvarProgressoPagina(1, 2, 120);

        verify(jdbc).update(anyString(), eq(120), eq(1), eq(2));
    }

    // ---------- AdminRepository (editoras) ----------

    @Test
    void editoraJaExistenteEhReaproveitada() {
        when(jdbc.queryForList(anyString(), eq(Integer.class), eq("Rocco"))).thenReturn(List.of(4));

        int id = new AdminRepository(jdbc).criarEditora(" Rocco ", null);

        org.junit.jupiter.api.Assertions.assertEquals(4, id);
        org.mockito.Mockito.verifyNoMoreInteractions(jdbc);
    }

    @Test
    void editoraNovaEhInseridaComPaisOpcional() {
        when(jdbc.queryForList(anyString(), eq(Integer.class), eq("Rocco"))).thenReturn(List.of());
        when(jdbc.queryForObject(anyString(), eq(Integer.class))).thenReturn(9);

        int id = new AdminRepository(jdbc).criarEditora("Rocco", "  ");

        org.junit.jupiter.api.Assertions.assertEquals(9, id);
        verify(jdbc).update(anyString(), eq("Rocco"), isNull());
    }
}
