package br.unisul.biblioteca.controller;

import br.unisul.biblioteca.model.Livro;
import br.unisul.biblioteca.repository.AvaliacaoRepository;
import br.unisul.biblioteca.repository.LivroRepository;
import br.unisul.biblioteca.security.UsuarioLogado;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class LivroController {

    private final LivroRepository livros;
    private final AvaliacaoRepository avaliacoes;

    public LivroController(LivroRepository livros, AvaliacaoRepository avaliacoes) {
        this.livros = livros;
        this.avaliacoes = avaliacoes;
    }

    @GetMapping({"/", "/livros"})
    public String catalogo(@RequestParam(required = false) String busca, Model model) {
        model.addAttribute("livros", livros.buscar(busca));
        model.addAttribute("busca", busca);
        return "catalogo";
    }

    @GetMapping("/livros/{id}")
    public String detalhe(@PathVariable int id,
                          @AuthenticationPrincipal UsuarioLogado usuario,
                          Model model) {
        Livro livro = livros.buscarPorId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado"));
        model.addAttribute("livro", livro);
        model.addAttribute("formatos", livros.formatosDaObra(id));
        model.addAttribute("avaliacoes", avaliacoes.listarPorObra(id));
        if (usuario != null) {
            model.addAttribute("minha", avaliacoes.doUsuario(usuario.getId(), id).orElse(null));
        }
        return "livro";
    }
}
