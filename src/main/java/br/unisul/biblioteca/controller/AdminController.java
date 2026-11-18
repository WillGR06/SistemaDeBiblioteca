package br.unisul.biblioteca.controller;

import br.unisul.biblioteca.util.ArmazenamentoCapas;
import br.unisul.biblioteca.util.Atributos;
import br.unisul.biblioteca.model.ObraForm;
import br.unisul.biblioteca.repository.AdminRepository;
import br.unisul.biblioteca.repository.LivroRepository;
import br.unisul.biblioteca.security.UsuarioLogado;
import br.unisul.biblioteca.util.Erros;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/** Área restrita a administradores (ver SecurityConfig). */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final String REDIRECT_OBRAS = "redirect:/admin/obras";
    private static final String TITULO_NOVA = "Nova obra";
    private static final String TITULO_EDITAR = "Editar obra";


    private final AdminRepository admin;
    private final LivroRepository livros;
    private final ArmazenamentoCapas capas;

    public AdminController(AdminRepository admin, LivroRepository livros, ArmazenamentoCapas capas) {
        this.admin = admin;
        this.livros = livros;
        this.capas = capas;
    }

    @GetMapping("/obras")
    public String listar(Model model) {
        model.addAttribute("livros", livros.buscar(null));
        return "admin/obras";
    }

    @GetMapping("/obras/nova")
    public String nova(Model model) {
        return formulario(model, ObraForm.vazio(), TITULO_NOVA);
    }

    @GetMapping("/obras/{id}/editar")
    public String editar(@PathVariable int id, Model model) {
        ObraForm form = admin.carregar(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Obra não encontrada"));
        return formulario(model, form, TITULO_EDITAR);
    }

    @PostMapping("/obras")
    public String salvar(@AuthenticationPrincipal UsuarioLogado usuario,
                         @ModelAttribute ObraForm form,
                         @RequestParam(value = "capa", required = false) MultipartFile capa,
                         Model model,
                         RedirectAttributes ra) {
        if (form.titulo() == null || form.titulo().isBlank()) {
            model.addAttribute(Atributos.ERRO, "Informe o título da obra.");
            return formulario(model, form, form.idObra() == null ? TITULO_NOVA : TITULO_EDITAR);
        }
        String novaCapa = null;
        try {
            if (capa != null && !capa.isEmpty()) {
                novaCapa = capas.salvar(capa);
            }
            String capaAntiga = (novaCapa != null && form.idObra() != null) ? admin.capaUrl(form.idObra()) : null;
            admin.salvar(form, usuario.getId(), novaCapa);
            capas.apagar(capaAntiga);
            ra.addFlashAttribute(Atributos.MSG, "Obra salva com sucesso.");
            return REDIRECT_OBRAS;
        } catch (Exception e) {
            capas.apagar(novaCapa);
            model.addAttribute(Atributos.ERRO, Erros.mensagem(e));
            return formulario(model, form, form.idObra() == null ? TITULO_NOVA : TITULO_EDITAR);
        }
    }

    @PostMapping("/obras/{id}/excluir")
    public String excluir(@AuthenticationPrincipal UsuarioLogado usuario,
                          @PathVariable int id,
                          RedirectAttributes ra) {
        try {
            admin.excluir(id, usuario.getId());
            ra.addFlashAttribute(Atributos.MSG, "Obra excluída.");
        } catch (Exception e) {
            ra.addFlashAttribute(Atributos.ERRO, "Não é possível excluir: existem empréstimos desta obra.");
        }
        return REDIRECT_OBRAS;
    }

    /** Cadastro rápido de editora (usado pelo botão "+ Nova editora" do formulário da obra). */
    @PostMapping("/editoras")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> novaEditora(@RequestParam String nome,
                                                           @RequestParam(required = false) String pais) {
        if (nome.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Informe o nome da editora."));
        }
        if (nome.trim().length() > 120 || (pais != null && pais.trim().length() > 60)) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Nome (até 120) ou país (até 60) muito longo."));
        }
        String nomeLimpo = nome.trim();
        int id = admin.criarEditora(nomeLimpo, pais);
        return ResponseEntity.ok(Map.of("id", id, "nome", nomeLimpo));
    }

    @GetMapping("/log")
    public String log(Model model) {
        model.addAttribute("logs", admin.logRecente());
        return "admin/log";
    }

    private String formulario(Model model, ObraForm form, String titulo) {
        model.addAttribute("form", form);
        model.addAttribute("tituloPagina", titulo);
        model.addAttribute("editoras", admin.editoras());
        model.addAttribute("capaAtual", form.idObra() == null ? null : admin.capaUrl(form.idObra()));
        return "admin/obra-form";
    }
}
