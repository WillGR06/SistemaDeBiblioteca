package br.unisul.biblioteca.controller;

import br.unisul.biblioteca.util.Atributos;
import br.unisul.biblioteca.repository.MembroRepository;
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

/** Gestão de membros. Acesso restrito a administradores (ver SecurityConfig: /admin/**). */
@Controller
@RequestMapping("/admin/membros")
public class MembroController {

    private static final String REDIRECT_MEMBROS = "redirect:/admin/membros";


    private final MembroRepository membros;

    public MembroController(MembroRepository membros) {
        this.membros = membros;
    }

    @GetMapping
    public String listar(@AuthenticationPrincipal UsuarioLogado usuario,
                         @RequestParam(required = false) String busca,
                         Model model) {
        model.addAttribute("membros", membros.listar(busca));
        model.addAttribute("busca", busca);
        model.addAttribute("meuId", usuario.getId());
        return "admin/membros";
    }

    @PostMapping("/{id}/status")
    public String status(@AuthenticationPrincipal UsuarioLogado usuario,
                         @PathVariable int id,
                         @RequestParam boolean ativo,
                         RedirectAttributes ra) {
        try {
            membros.alterarStatus(id, ativo, usuario.getId());
            ra.addFlashAttribute(Atributos.MSG, ativo ? "Membro ativado." : "Membro desativado.");
        } catch (Exception e) {
            ra.addFlashAttribute(Atributos.ERRO, Erros.mensagem(e));
        }
        return REDIRECT_MEMBROS;
    }

    @PostMapping("/{id}/perfil")
    public String perfil(@AuthenticationPrincipal UsuarioLogado usuario,
                         @PathVariable int id,
                         @RequestParam String perfil,
                         RedirectAttributes ra) {
        try {
            membros.alterarPerfil(id, perfil, usuario.getId());
            ra.addFlashAttribute(Atributos.MSG, "Perfil alterado.");
        } catch (Exception e) {
            ra.addFlashAttribute(Atributos.ERRO, Erros.mensagem(e));
        }
        return REDIRECT_MEMBROS;
    }
}
