package br.unisul.biblioteca.model;

/** Linha da tela de gestão de membros (administrador). */
public record Membro(
        int id,
        String nome,
        String email,
        String perfil,
        boolean ativo,
        String cadastro,
        int totalEmprestimos) {
}
