package br.unisul.biblioteca;

import br.unisul.biblioteca.util.Erros;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Testes unitários da conversão de erros (não precisam de banco). */
class ErrosTest {

    @Test
    void mensagemDaProcedureEhRepassadaAoUsuario() {
        SQLException sql = new SQLException("Você já possui um empréstimo ativo deste livro", "45000");
        RuntimeException envolvida = new RuntimeException(sql);

        assertEquals("Você já possui um empréstimo ativo deste livro", Erros.mensagem(envolvida));
    }

    @Test
    void erroDesconhecidoViraMensagemGenerica() {
        assertEquals("Não foi possível concluir a operação. Tente novamente.",
                Erros.mensagem(new RuntimeException("falha qualquer")));
    }

    @Test
    void argumentoInvalidoMostraAPropriaMensagem() {
        assertEquals("Empréstimo não encontrado.",
                Erros.mensagem(new IllegalArgumentException("Empréstimo não encontrado.")));
    }
}
