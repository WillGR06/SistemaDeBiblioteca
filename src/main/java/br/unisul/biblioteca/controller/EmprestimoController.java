package br.unisul.biblioteca.controller;

import br.unisul.biblioteca.util.Atributos;
import br.unisul.biblioteca.repository.EmprestimoRepository;
import br.unisul.biblioteca.security.UsuarioLogado;
import br.unisul.biblioteca.util.Erros;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/emprestimos")
public class EmprestimoController {

    private static final String REDIRECT_EMPRESTIMOS = "redirect:/emprestimos";


    private final EmprestimoRepository emprestimos;

    public EmprestimoController(EmprestimoRepository emprestimos) {
        this.emprestimos = emprestimos;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal UsuarioLogado usuario, Model model) {
        model.addAttribute("emprestimos", emprestimos.listarPorUsuario(usuario.getId()));
        return "emprestimos";
    }

    @PostMapping
    public String emprestar(@AuthenticationPrincipal UsuarioLogado usuario,
                            @RequestParam(required = false) Integer idEbook,
                            @RequestParam(required = false) Integer idAudiobook,
                            @RequestParam String forma,
                            RedirectAttributes ra) {
        try {
            emprestimos.realizar(usuario.getId(), idEbook, idAudiobook, forma);
            ra.addFlashAttribute(Atributos.MSG, "Empréstimo realizado! Você tem 30 dias para ler.");
        } catch (Exception e) {
            ra.addFlashAttribute(Atributos.ERRO, Erros.mensagem(e));
        }
        return REDIRECT_EMPRESTIMOS;
    }

    @PostMapping("/{id}/devolver")
    public String devolver(@AuthenticationPrincipal UsuarioLogado usuario,
                           @PathVariable int id,
                           RedirectAttributes ra) {
        try {
            emprestimos.devolver(id, usuario.getId());
            ra.addFlashAttribute(Atributos.MSG, "Livro devolvido. O acesso foi bloqueado.");
        } catch (Exception e) {
            ra.addFlashAttribute(Atributos.ERRO, Erros.mensagem(e));
        }
        return REDIRECT_EMPRESTIMOS;
    }

    @GetMapping("/{id}/ler")
    public String ler(@AuthenticationPrincipal UsuarioLogado usuario,
                      @PathVariable int id,
                      Model model,
                      RedirectAttributes ra) {
        return emprestimos.acessoLiberado(id, usuario.getId())
                .map(acesso -> {
                    model.addAttribute("acesso", acesso);
                    return "leitura";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute(Atributos.ERRO, "Acesso bloqueado: o empréstimo expirou ou foi devolvido.");
                    return REDIRECT_EMPRESTIMOS;
                });
    }

    @PostMapping("/{id}/progresso")
    public String progresso(@AuthenticationPrincipal UsuarioLogado usuario,
                            @PathVariable int id,
                            @RequestParam double percentual) {
        emprestimos.salvarProgresso(id, usuario.getId(), percentual);
        return "redirect:/emprestimos/" + id + "/ler";
    }

    @PostMapping("/{id}/pagina")
    public String progressoPorPagina(@AuthenticationPrincipal UsuarioLogado usuario,
                                     @PathVariable int id,
                                     @RequestParam int pagina) {
        emprestimos.salvarProgressoPagina(id, usuario.getId(), pagina);
        return "redirect:/emprestimos/" + id + "/ler";
    }
}
