package br.unisul.biblioteca.util;

import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;

/** Converte exceções técnicas em mensagens amigáveis para o usuário. */
public final class Erros {

    private Erros() {
    }

    public static String mensagem(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            // SIGNAL SQLSTATE '45000' das procedures e triggers: a mensagem já é amigável
            if (t instanceof SQLException sql && "45000".equals(sql.getSQLState())) {
                return sql.getMessage();
            }
            if (t instanceof IllegalArgumentException) {
                return t.getMessage();
            }
        }
        if (e instanceof DataIntegrityViolationException) {
            return "Operação não permitida: o registro está em uso ou os dados são inválidos.";
        }
        return "Não foi possível concluir a operação. Tente novamente.";
    }
}
