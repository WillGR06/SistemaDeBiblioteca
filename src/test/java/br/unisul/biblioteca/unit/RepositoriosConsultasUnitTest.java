package br.unisul.biblioteca.unit;

import br.unisul.biblioteca.model.Acesso;
import br.unisul.biblioteca.model.AvaliacaoResumo;
import br.unisul.biblioteca.model.EmprestimoResumo;
import br.unisul.biblioteca.model.Formato;
import br.unisul.biblioteca.model.Livro;
import br.unisul.biblioteca.model.Membro;
import br.unisul.biblioteca.model.Usuario;
import br.unisul.biblioteca.repository.AvaliacaoRepository;
import br.unisul.biblioteca.repository.EmprestimoRepository;
import br.unisul.biblioteca.repository.LivroRepository;
import br.unisul.biblioteca.repository.MembroRepository;
import br.unisul.biblioteca.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Captor;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Mapeamento de linhas e caminhos alternativos dos repositórios de consulta (sem banco). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RepositoriosConsultasUnitTest {

    @Mock
    private JdbcTemplate jdbc;

    @Captor
    private ArgumentCaptor<RowMapper<Livro>> mapperLivro;
    @Captor
    private ArgumentCaptor<RowMapper<Formato>> mapperFormato;
    @Captor
    private ArgumentCaptor<RowMapper<Usuario>> mapperUsuario;
    @Captor
    private ArgumentCaptor<RowMapper<Membro>> mapperMembro;
    @Captor
    private ArgumentCaptor<RowMapper<EmprestimoResumo>> mapperResumo;
    @Captor
    private ArgumentCaptor<RowMapper<Acesso>> mapperAcesso;
    @Captor
    private ArgumentCaptor<RowMapper<AvaliacaoResumo>> mapperAvaliacao;

    // ---------- LivroRepository ----------

    @Test
    void buscaVaziaOuNulaListaTudoEBuscaComTextoRemoveEspacos() {
        LivroRepository repo = new LivroRepository(jdbc);

        repo.buscar(null);
        repo.buscar("  ana ");

        verify(jdbc).query(anyString(), mapperLivro.capture(), eq("%%"), eq("%%"));
        verify(jdbc).query(anyString(), mapperLivro.capture(), eq("%ana%"), eq("%ana%"));
    }

    @Test
    void livroSemAnoEhMapeadoComAnoNulo() throws SQLException {
        new LivroRepository(jdbc).buscarPorId(3);
        verify(jdbc).query(anyString(), mapperLivro.capture(), eq(3));
        ResultSet rs = mock(ResultSet.class);
        when(rs.getInt("ano_publicacao")).thenReturn(0);
        when(rs.wasNull()).thenReturn(true);
        when(rs.getString("titulo")).thenReturn("Sem ano");

        Livro livro = mapperLivro.getValue().mapRow(rs, 0);

        assertNull(livro.anoPublicacao());
        assertEquals("Sem ano", livro.titulo());
    }

    @Test
    void livroComAnoEhMapeadoComOAno() throws SQLException {
        new LivroRepository(jdbc).buscarPorId(3);
        verify(jdbc).query(anyString(), mapperLivro.capture(), eq(3));
        ResultSet rs = mock(ResultSet.class);
        when(rs.getInt("ano_publicacao")).thenReturn(1899);
        when(rs.wasNull()).thenReturn(false);
        when(rs.getString("capa_url")).thenReturn("/capas/a.png");
        when(rs.getBoolean("tem_ebook")).thenReturn(true);

        Livro livro = mapperLivro.getValue().mapRow(rs, 0);

        assertEquals(1899, livro.anoPublicacao());
        assertTrue(livro.temCapa());
        assertTrue(livro.temEbook());
    }

    @Test
    void formatosDaObraSaoMapeadosDasColunas() throws SQLException {
        new LivroRepository(jdbc).formatosDaObra(3);
        verify(jdbc).query(anyString(), mapperFormato.capture(), eq(3), eq(3));
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("tipo")).thenReturn("ebook");
        when(rs.getInt("id")).thenReturn(8);
        when(rs.getBigDecimal("valor_emprestimo")).thenReturn(new BigDecimal("9.90"));

        Formato formato = mapperFormato.getValue().mapRow(rs, 0);

        assertNotNull(formato);
    }

    // ---------- UsuarioRepository ----------

    @Test
    void emailExisteSoQuandoAContagemEhMaiorQueZero() {
        UsuarioRepository repo = new UsuarioRepository(jdbc);
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq("nulo@x.com"))).thenReturn(null);
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq("zero@x.com"))).thenReturn(0);
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq("um@x.com"))).thenReturn(1);

        assertFalse(repo.existeEmail("nulo@x.com"));
        assertFalse(repo.existeEmail("zero@x.com"));
        assertTrue(repo.existeEmail("um@x.com"));
    }

    @Test
    void usuarioEhMapeadoPelasColunasEcriarGravaOsTresCampos() throws SQLException {
        UsuarioRepository repo = new UsuarioRepository(jdbc);

        assertTrue(repo.porEmail("a@b.com").isEmpty());
        verify(jdbc).query(anyString(), mapperUsuario.capture(), eq("a@b.com"));
        ResultSet rs = mock(ResultSet.class);
        when(rs.getInt(1)).thenReturn(7);
        when(rs.getString(2)).thenReturn("Ana");
        when(rs.getBoolean(6)).thenReturn(true);
        assertNotNull(mapperUsuario.getValue().mapRow(rs, 0));

        repo.criar("Ana", "a@b.com", "hash");
        verify(jdbc).update(anyString(), eq("Ana"), eq("a@b.com"), eq("hash"));
    }

    // ---------- MembroRepository ----------

    @Test
    void listarMembrosSemBuscaOuComBuscaEMapeiaAsColunas() throws SQLException {
        MembroRepository repo = new MembroRepository(jdbc);

        repo.listar(null);
        repo.listar(" ana ");

        verify(jdbc).query(anyString(), mapperMembro.capture(), eq("%%"), eq("%%"));
        verify(jdbc).query(anyString(), mapperMembro.capture(), eq("%ana%"), eq("%ana%"));
        ResultSet rs = mock(ResultSet.class);
        when(rs.getInt("id_usuario")).thenReturn(5);
        when(rs.getString("nome")).thenReturn("Ana");
        assertNotNull(mapperMembro.getValue().mapRow(rs, 0));
    }

    @Test
    void ativarMembroGravaNoHistoricoComTextoDeAtivacao() {
        when(jdbc.queryForList(anyString(), eq(String.class), eq(5))).thenReturn(List.of("Fulano"));

        new MembroRepository(jdbc).alterarStatus(5, true, 1);

        verify(jdbc).update(anyString(), eq(true), eq(5));
        verify(jdbc).update(anyString(), eq(1), eq(5), eq("Membro ativado: Fulano"));
    }

    // ---------- EmprestimoRepository ----------

    private Connection conexaoComProcedure(CallableStatement cs) throws SQLException {
        Connection con = mock(Connection.class);
        when(con.prepareCall(anyString())).thenReturn(cs);
        return con;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void executarCallbackNa(Connection con) throws SQLException {
        when(jdbc.execute(any(ConnectionCallback.class)))
                .thenAnswer(inv -> ((ConnectionCallback) inv.getArgument(0)).doInConnection(con));
    }

    @Test
    void realizarEmprestimoDeEbookEnviaNuloNoAudiobook() throws SQLException {
        CallableStatement cs = mock(CallableStatement.class);
        when(cs.getInt(5)).thenReturn(42);
        executarCallbackNa(conexaoComProcedure(cs));

        int id = new EmprestimoRepository(jdbc).realizar(1, 10, null, "pix");

        assertEquals(42, id);
        verify(cs).setInt(2, 10);
        verify(cs).setNull(3, Types.INTEGER);
        verify(cs).setString(4, "pix");
    }

    @Test
    void realizarEmprestimoDeAudiobookEnviaNuloNoEbook() throws SQLException {
        CallableStatement cs = mock(CallableStatement.class);
        when(cs.getInt(5)).thenReturn(43);
        executarCallbackNa(conexaoComProcedure(cs));

        int id = new EmprestimoRepository(jdbc).realizar(1, null, 20, "cartao");

        assertEquals(43, id);
        verify(cs).setNull(2, Types.INTEGER);
        verify(cs).setInt(3, 20);
    }

    @Test
    void realizarEmprestimoSemRetornoDoBancoDevolveZero() {
        when(jdbc.execute(any(ConnectionCallback.class))).thenReturn(null);

        assertEquals(0, new EmprestimoRepository(jdbc).realizar(1, 10, null, "pix"));
    }

    @Test
    void devolverEmprestimoDoProprioUsuarioChamaAProcedure() throws SQLException {
        CallableStatement cs = mock(CallableStatement.class);
        executarCallbackNa(conexaoComProcedure(cs));
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(9), eq(1))).thenReturn(1);

        new EmprestimoRepository(jdbc).devolver(9, 1);

        verify(cs).setInt(1, 9);
        verify(cs).execute();
    }

    @Test
    void devolverComContagemNulaEhRecusado() {
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(9), eq(1))).thenReturn(null);
        EmprestimoRepository repo = new EmprestimoRepository(jdbc);

        assertThrows(IllegalArgumentException.class, () -> repo.devolver(9, 1));
        verify(jdbc, never()).execute(any(ConnectionCallback.class));
    }

    @Test
    void emprestimoEhMapeadoNaListaDoUsuario() throws SQLException {
        new EmprestimoRepository(jdbc).listarPorUsuario(2);
        verify(jdbc).query(anyString(), mapperResumo.capture(), eq(2));
        ResultSet rs = mock(ResultSet.class);
        when(rs.getInt("id_emprestimo")).thenReturn(4);
        when(rs.getString("situacao")).thenReturn("Emprestado");

        assertNotNull(mapperResumo.getValue().mapRow(rs, 0));
    }

    private Acesso mapearAcesso(int total, boolean eraNulo, double progresso) throws SQLException {
        new EmprestimoRepository(jdbc).acessoLiberado(1, 2);
        verify(jdbc).query(anyString(), mapperAcesso.capture(), eq(1), eq(2));
        ResultSet rs = mock(ResultSet.class);
        when(rs.getDouble("progresso")).thenReturn(progresso);
        when(rs.getInt("total_paginas")).thenReturn(total);
        when(rs.wasNull()).thenReturn(eraNulo);
        return mapperAcesso.getValue().mapRow(rs, 0);
    }

    @Test
    void acessoComPaginasConverteOPercentualEmPagina() throws SQLException {
        Acesso acesso = mapearAcesso(250, false, 40.0);

        assertTrue(acesso.porPagina());
        assertEquals(250, acesso.totalPaginas());
        assertEquals(100, acesso.paginaAtual());
    }

    @Test
    void acessoSemTotalDePaginasFicaEmPercentual() throws SQLException {
        Acesso acesso = mapearAcesso(0, true, 40.0);

        assertFalse(acesso.porPagina());
        assertEquals(0, acesso.paginaAtual());
    }

    @Test
    void acessoComTotalZeroNaoEhPorPagina() throws SQLException {
        Acesso acesso = mapearAcesso(0, false, 40.0);

        assertFalse(acesso.porPagina());
    }

    // ---------- AvaliacaoRepository ----------

    @Test
    void avaliacoesDaObraEDoUsuarioSaoMapeadas() throws SQLException {
        AvaliacaoRepository repo = new AvaliacaoRepository(jdbc);

        repo.listarPorObra(4);
        verify(jdbc).query(anyString(), mapperAvaliacao.capture(), eq(4));
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("nome")).thenReturn("Ana");
        when(rs.getInt("nota")).thenReturn(5);
        assertNotNull(mapperAvaliacao.getValue().mapRow(rs, 0));

        assertTrue(repo.doUsuario(4, 7).isEmpty());   // usuário 4, obra 7
        verify(jdbc).query(anyString(), mapperAvaliacao.capture(), eq(7), eq(4));   // o SQL recebe obra e depois usuário
    }
}
