package br.unisul.biblioteca.model;

public record Usuario(int id, String nome, String email, String senhaHash, String perfil, boolean ativo) {
}
