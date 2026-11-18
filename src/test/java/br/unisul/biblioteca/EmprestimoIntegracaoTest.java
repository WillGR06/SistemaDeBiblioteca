package br.unisul.biblioteca;

import br.unisul.biblioteca.model.EmprestimoResumo;
import br.unisul.biblioteca.repository.EmprestimoRepository;
import br.unisul.biblioteca.util.Erros;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmprestimoIntegracaoTest extends BaseIntegracaoTest {

    @Autowired
    private EmprestimoRepository emprestimos;

    @Test
    void emprestarEbookCriaEmprestimoPagamentoEAcesso() {
        int id = emprestimos.realizar(idLeitor, idEbook, null, "pix");

        assertTrue(id > 0);
        Map<String, Object> e = jdbc.queryForMap("""
                SELECT status, valor, TIMESTAMPDIFF(DAY, data_emprestimo, data_vencimento) AS dias
                  FROM emprestimo WHERE id_emprestimo = ?
                """, id);
        assertEquals("ativo", e.get("status"));
        assertEquals(30L, ((Number) e.get("dias")).longValue(), "O prazo deve ser de 30 dias");
        assertEquals(0, new BigDecimal("9.90").compareTo((BigDecimal) e.get("valor")));

        assertEquals(1, contar("SELECT COUNT(*) FROM pagamento WHERE id_emprestimo = ? AND status = 'aprovado'", id));
        assertEquals(1, contar("SELECT COUNT(*) FROM acesso_digital WHERE id_emprestimo = ?", id));
    }

    @Test
    void emprestarAudiobookCobraOValorDoAudiobook() {
        int id = emprestimos.realizar(idLeitor, null, idAudiobook, "cartao_credito");

        BigDecimal valor = jdbc.queryForObject("SELECT valor FROM emprestimo WHERE id_emprestimo = ?",
                BigDecimal.class, id);
        assertEquals(0, new BigDecimal("14.90").compareTo(valor));
    }

    @Test
    void emprestimoRepetidoDoMesmoLivroEhRecusado() {
        emprestimos.realizar(idLeitor, idEbook, null, "pix");

        RuntimeException erro = assertThrows(RuntimeException.class,
                () -> emprestimos.realizar(idLeitor, idEbook, null, "pix"));

        assertTrue(Erros.mensagem(erro).contains("ativo deste livro"), Erros.mensagem(erro));
        assertEquals(1, contar("SELECT COUNT(*) FROM emprestimo WHERE id_usuario = ?", idLeitor));
    }

    @Test
    void outroUsuarioPodePegarOMesmoLivro() {
        emprestimos.realizar(idLeitor, idEbook, null, "pix");
        int id = emprestimos.realizar(idOutroLeitor, idEbook, null, "pix");

        assertTrue(id > 0);
    }

    @Test
    void semInformarLivroEhRecusado() {
        RuntimeException erro = assertThrows(RuntimeException.class,
                () -> emprestimos.realizar(idLeitor, null, null, "pix"));

        assertTrue(Erros.mensagem(erro).contains("Informe apenas um"), Erros.mensagem(erro));
        assertEquals(0, contar("SELECT COUNT(*) FROM emprestimo"));
    }

    @Test
    void informarEbookEAudiobookJuntosEhRecusado() {
        assertThrows(RuntimeException.class,
                () -> emprestimos.realizar(idLeitor, idEbook, idAudiobook, "pix"));
    }

    @Test
    void devolverMarcaDevolvidoEBloqueiaOAcesso() {
        int id = emprestimos.realizar(idLeitor, idEbook, null, "pix");
        assertTrue(emprestimos.acessoLiberado(id, idLeitor).isPresent());

        emprestimos.devolver(id, idLeitor);

        Map<String, Object> e = jdbc.queryForMap(
                "SELECT status, data_devolucao, data_bloqueio FROM emprestimo WHERE id_emprestimo = ?", id);
        assertEquals("devolvido", e.get("status"));
        assertTrue(e.get("data_devolucao") != null && e.get("data_bloqueio") != null);
        assertTrue(emprestimos.acessoLiberado(id, idLeitor).isEmpty(), "Depois de devolver, o acesso deve estar bloqueado");
    }

    @Test
    void naoEhPossivelDevolverDuasVezes() {
        int id = emprestimos.realizar(idLeitor, idEbook, null, "pix");
        emprestimos.devolver(id, idLeitor);

        RuntimeException erro = assertThrows(RuntimeException.class, () -> emprestimos.devolver(id, idLeitor));

        assertTrue(Erros.mensagem(erro).contains("encerrado"), Erros.mensagem(erro));
    }

    @Test
    void usuarioNaoPodeDevolverEmprestimoDeOutro() {
        int id = emprestimos.realizar(idLeitor, idEbook, null, "pix");

        assertThrows(IllegalArgumentException.class, () -> emprestimos.devolver(id, idOutroLeitor));
        assertEquals("ativo", jdbc.queryForObject("SELECT status FROM emprestimo WHERE id_emprestimo = ?",
                String.class, id));
    }

    @Test
    void emprestimoVencidoFicaBloqueado() {
        int id = emprestimos.realizar(idLeitor, idEbook, null, "pix");
        jdbc.update("UPDATE emprestimo SET data_vencimento = NOW() - INTERVAL 1 DAY WHERE id_emprestimo = ?", id);

        assertTrue(emprestimos.acessoLiberado(id, idLeitor).isEmpty(), "Vencido não pode liberar leitura");
        assertEquals(0, jdbc.queryForObject("SELECT fn_dias_restantes(?)", Integer.class, id));

        List<EmprestimoResumo> lista = emprestimos.listarPorUsuario(idLeitor);
        assertEquals("Bloqueado", lista.get(0).situacao());
    }

    @Test
    void funcaoDiasRestantesRetornaPrazoDeUmEmprestimoNovo() {
        int id = emprestimos.realizar(idLeitor, idEbook, null, "pix");

        int dias = jdbc.queryForObject("SELECT fn_dias_restantes(?)", Integer.class, id);

        assertTrue(dias >= 29 && dias <= 30, "Esperado 29 ou 30, veio " + dias);
    }

    @Test
    void progressoPorPaginaRespeitaOTotalDePaginasDoLivro() {
        int id = emprestimos.realizar(idLeitor, idEbook, null, "pix");

        emprestimos.salvarProgressoPagina(id, idLeitor, 128);
        var acesso = emprestimos.acessoLiberado(id, idLeitor).orElseThrow();
        assertEquals(256, acesso.totalPaginas());
        assertEquals(128, acesso.paginaAtual());
        assertEquals(50.0, acesso.progresso(), 0.001);

        emprestimos.salvarProgressoPagina(id, idLeitor, 9999);
        assertEquals(256, emprestimos.acessoLiberado(id, idLeitor).orElseThrow().paginaAtual());

        emprestimos.salvarProgressoPagina(id, idLeitor, -5);
        assertEquals(0, emprestimos.acessoLiberado(id, idLeitor).orElseThrow().paginaAtual());
    }

    @Test
    void progressoDeLeituraEhSalvoSomenteComEmprestimoAtivo() {
        int id = emprestimos.realizar(idLeitor, idEbook, null, "pix");

        emprestimos.salvarProgresso(id, idLeitor, 30);
        assertEquals(30.0, emprestimos.acessoLiberado(id, idLeitor).orElseThrow().progresso(), 0.001);

        emprestimos.devolver(id, idLeitor);
        emprestimos.salvarProgresso(id, idLeitor, 80);
        Double progresso = jdbc.queryForObject(
                "SELECT progresso_percentual FROM acesso_digital WHERE id_emprestimo = ?", Double.class, id);
        assertEquals(30.0, progresso, 0.001, "Após a devolução o progresso não pode mudar");
    }

    @Test
    void historicoMostraEmprestadoEDevolvido() {
        int id1 = emprestimos.realizar(idLeitor, idEbook, null, "pix");
        emprestimos.realizar(idLeitor, null, idAudiobook, "pix");
        emprestimos.devolver(id1, idLeitor);

        List<EmprestimoResumo> lista = emprestimos.listarPorUsuario(idLeitor);

        assertEquals(2, lista.size());
        assertTrue(lista.stream().anyMatch(e -> e.situacao().equals("Emprestado")));
        assertTrue(lista.stream().anyMatch(e -> e.situacao().equals("Devolvido")));
    }
}
