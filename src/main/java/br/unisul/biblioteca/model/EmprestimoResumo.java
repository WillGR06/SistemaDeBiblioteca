package br.unisul.biblioteca.model;

/** Linha da tela "Meus empréstimos". As datas já vêm formatadas do banco. */
public record EmprestimoResumo(
        int id,
        String titulo,
        String tipo,
        String emprestadoEm,
        String vencimento,
        String bloqueadoEm,
        String devolvidoEm,
        String situacao,
        int diasRestantes) {
}
