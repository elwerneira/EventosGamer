package Informacion.Eventos;

import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class LoginController {
    private final AccountService accounts;

    public LoginController(AccountService accounts) {
        this.accounts = accounts;
    }

    @GetMapping("/login")
    public String login() {
        return "Login";
    }

    @GetMapping("/registro")
    public String registration(Model model) {
        model.addAttribute("form", new RegistrationForm("", "", ""));
        return "Registro";
    }

    @PostMapping("/registro")
    public String register(@Valid @ModelAttribute("form") RegistrationForm form, BindingResult errors) {
        if (form.password() != null && form.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            errors.rejectValue("password", "length", "La contraseña supera el límite de 72 bytes de BCrypt.");
        }
        if (!errors.hasErrors()) {
            try {
                accounts.register(form);
                return "redirect:/login?registered";
            } catch (DataIntegrityViolationException exception) {
                errors.rejectValue("email", "duplicate", "No se pudo registrar ese correo. Usa otro o inicia sesión.");
            }
        }
        return "Registro";
    }

    @GetMapping("/perfil")
    public String profile(Principal principal, Model model) {
        model.addAttribute("form", accounts.profile(principal.getName()));
        return "Perfil";
    }

    @PostMapping("/perfil")
    public String saveProfile(Principal principal, @Valid @ModelAttribute("form") ProfileForm form,
            BindingResult errors) {
        if (form == null || errors.hasFieldErrors("notifications")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        if (errors.hasErrors()) {
            return "Perfil";
        }
        accounts.updateProfile(principal.getName(), form);
        return "redirect:/perfil?saved";
    }
}
