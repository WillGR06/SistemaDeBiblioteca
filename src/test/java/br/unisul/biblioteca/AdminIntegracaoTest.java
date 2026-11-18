package br.unisul.biblioteca;

import br.unisul.biblioteca.model.ObraForm;
import br.unisul.biblioteca.repository.AdminRepository;
import br.unisul.biblioteca.repository.EmprestimoRepository;
import br.unisul.biblioteca.util.Erros;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminIntegracaoTest extends BaseIntegracaoTest {

    @Autowired
    private AdminRepository admin;

    @Autowired
    private EmprestimoRepository emprestimos;

    private ObraForm novoForm(String titulo, BigDecimal valorEbook) {
        return new ObraForm(null, titulo, "Sinopse", "Português", 2020, null, "Autor Novo",
                "PDF", "/arquivos/novo.pdf", valorEbook, 100,
                "", null, "MP3", "", null);
    }

    @Test
    void cadastrarObraRegistraAdministradorELog() {
        int id = admin.salvar(novoForm("Livro Novo", new BigDecimal("8.90")), idAdmin);

        assertEquals(idAdmin, jdbc.queryForObject(
                "SELECT id_admin_cadastro FROM obra WHERE id_obra = ?", Integer.class, id));
        assertEquals(1, contar("""
                SELECT COUNT(*) FROM log_administracao
                 WHERE tabela_afetada = 'obra' AND acao = 'inserir' AND id_administrador = ? AND id_registro = ?
                """, idAdmin, id));
        assertEquals(1, contar("SELECT COUNT(*) FROM ebook WHERE id_obra = ?", id));
        assertEquals(1, contar("SELECT COUNT(*) FROM autor WHERE nome = 'Autor Novo'"));
    }

    @Test
    void editarObraAtualizaDadosEGravaLog() {
        int id = admin.salvar(novoForm("Livro Para Editar", new BigDecimal("8.90")), idAdmin);
        ObraForm f = admin.carregar(id).orElseThrow();

        admin.salvar(new ObraForm(id, "Livro Editado", f.sinopse(), f.idioma(), f.anoPublicacao(), f.idEditora(),
                f.autor(), f.ebookFormato(), f.ebookUrl(), new BigDecimal("10.00"), f.ebookPaginas(),
                f.audioNarrador(), f.audioDuracao(), f.audioFormato(), f.audioUrl(), f.audioValor()), idAdmin);

        assertEquals("Livro Editado", jdbc.queryForObject(
                "SELECT titulo FROM obra WHERE id_obra = ?", String.class, id));
        BigDecimal valor = jdbc.queryForObject(
                "SELECT valor_emprestimo FROM ebook WHERE id_obra = ?", BigDecimal.class, id);
        assertEquals(0, new BigDecimal("10.00").compareTo(valor));
        assertTrue(contar("SELECT COUNT(*) FROM log_administracao WHERE tabela_afetada = 'obra' AND acao = 'editar'") >= 1);
    }

    @Test
    void valorNegativoEhRecusadoESemDeixarSujeira() {
        RuntimeException erro = assertThrows(RuntimeException.class,
                () -> admin.salvar(novoForm("Livro Invalido", new BigDecimal("-1")), idAdmin));

        assertTrue(Erros.mensagem(erro).contains("negativo"), Erros.mensagem(erro));
        assertEquals(0, contar("SELECT COUNT(*) FROM obra WHERE titulo = 'Livro Invalido'"),
                "A transação deve ser desfeita (rollback)");
    }

    @Test
    void excluirObraSemEmprestimosGravaLog() {
        admin.excluir(idObra, idAdmin);

        assertEquals(0, contar("SELECT COUNT(*) FROM obra WHERE id_obra = ?", idObra));
        assertEquals(1, contar("""
                SELECT COUNT(*) FROM log_administracao
                 WHERE tabela_afetada = 'obra' AND acao = 'excluir' AND id_administrador = ?
                """, idAdmin));
    }

    @Test
    void excluirObraComEmprestimoEhRecusado() {
        emprestimos.realizar(idLeitor, idEbook, null, "pix");

        assertThrows(RuntimeException.class, () -> admin.excluir(idObra, idAdmin));

        assertEquals(1, contar("SELECT COUNT(*) FROM obra WHERE id_obra = ?", idObra),
                "A obra com empréstimos deve continuar existindo");
    }

    @Test
    void semAdministradorNaConexaoNaoHaLog() {
        // Inserção direta (sem passar pelo AdminRepository) não define @id_admin
        jdbc.update("INSERT INTO obra (titulo) VALUES ('Obra Direta')");

        assertEquals(0, contar("SELECT COUNT(*) FROM log_administracao WHERE descricao LIKE '%Obra Direta%'"));
    }

    @Test
    void criarEditoraGravaNoBancoEReaproveitaNomeRepetido() {
        int id = admin.criarEditora("Editora Nova", "Brasil");

        assertEquals(1, contar("SELECT COUNT(*) FROM editora WHERE id_editora = ? AND pais = 'Brasil'", id));
        assertEquals(id, admin.criarEditora("Editora Nova", null));
        assertEquals(1, contar("SELECT COUNT(*) FROM editora WHERE nome = 'Editora Nova'"));
    }

    @Test
    void capaEhGravadaMantidaEtrocada() {
        int id = admin.salvar(novoForm("Livro Com Capa", new BigDecimal("8.90")), idAdmin, "/capas/a.png");
        assertEquals("/capas/a.png", admin.capaUrl(id));

        ObraForm f = admin.carregar(id).orElseThrow();
        admin.salvar(f, idAdmin, null);
        assertEquals("/capas/a.png", admin.capaUrl(id), "Sem nova imagem, a capa atual deve ser mantida");

        admin.salvar(f, idAdmin, "/capas/b.png");
        assertEquals("/capas/b.png", admin.capaUrl(id));
    }
}
