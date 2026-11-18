package br.unisul.biblioteca.model;

import java.math.BigDecimal;

/** Formato digital de uma obra disponível para empréstimo (e-book ou audiobook). */
public record Formato(String tipo, int id, String formato, BigDecimal valor) {
}
