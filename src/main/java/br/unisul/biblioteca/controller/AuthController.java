package br.unisul.biblioteca.controller;

import br.unisul.biblioteca.util.Atributos;
import br.unisul.biblioteca.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public AuthController(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/cadastro")
    public String cadastro() {
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(@RequestParam String nome,
                            @RequestParam String email,
                            @RequestParam String senha,
                            @RequestParam String confirmar,
                            Model model,
                            RedirectAttributes ra) {
        String emailNormalizado = email.trim().toLowerCase();
        String erro = null;
        if (nome.isBlank()) {
            erro = "Informe seu nome.";
        } else if (!emailNormalizado.contains("@")) {
            erro = "Informe um e-mail válido.";
        } else if (senha.length() < 6) {
            erro = "A senha deve ter pelo menos 6 caracteres.";
        } else if (!senha.equals(confirmar)) {
            erro = "As senhas não conferem.";
        } else if (usuarios.existeEmail(emailNormalizado)) {
            erro = "Este e-mail já está cadastrado.";
        }
        if (erro != null) {
            model.addAttribute(Atributos.ERRO, erro);
            model.addAttribute("nome", nome);
            model.addAttribute("email", email);
            return "cadastro";
        }
        usuarios.criar(nome.trim(), emailNormalizado, encoder.encode(senha));
        ra.addFlashAttribute(Atributos.MSG, "Cadastro realizado! Faça login para continuar.");
        return "redirect:/login";
    }
}
