package br.unisul.biblioteca.controller;

import br.unisul.biblioteca.util.Atributos;
import br.unisul.biblioteca.repository.AvaliacaoRepository;
import br.unisul.biblioteca.repository.LivroRepository;
import br.unisul.biblioteca.security.UsuarioLogado;
import br.unisul.biblioteca.util.Erros;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AvaliacaoController {

    private final AvaliacaoRepository avaliacoes;
    private final LivroRepository livros;

    public AvaliacaoController(AvaliacaoRepository avaliacoes, LivroRepository livros) {
        this.avaliacoes = avaliacoes;
        this.livros = livros;
    }

    @PostMapping("/livros/{id}/avaliar")
    public String avaliar(@AuthenticationPrincipal UsuarioLogado usuario,
                          @PathVariable int id,
                          @RequestParam int nota,
                          @RequestParam(required = false) String comentario,
                          RedirectAttributes ra) {
        if (livros.buscarPorId(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Livro não encontrado");
        }
        try {
            avaliacoes.salvar(usuario.getId(), id, nota, comentario);
            ra.addFlashAttribute(Atributos.MSG, "Avaliação salva. Obrigado!");
        } catch (Exception e) {
            ra.addFlashAttribute(Atributos.ERRO, Erros.mensagem(e));
        }
        return "redirect:/livros/" + id;
    }
}
