package Informacion.Eventos;

import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/admin/usuarios")
public class AdminAccountController {
    private final AdminAccountService accounts;

    public AdminAccountController(AdminAccountService accounts) { this.accounts = accounts; }

    @GetMapping
    String list(Model model) {
        model.addAttribute("accounts", accounts.list());
        return "AdminUsuarios";
    }

    @GetMapping("/nuevo")
    String createForm(Model model) {
        model.addAttribute("form", new AdminAccountCreateForm("", "", "", AccountRole.Role.USER));
        return "AdminUsuarioNuevo";
    }

    @PostMapping("/nuevo")
    String create(@Valid @ModelAttribute("form") AdminAccountCreateForm form, BindingResult errors) {
        if (!errors.hasErrors()) {
            try {
                accounts.create(form);
                return "redirect:/admin/usuarios?created";
            } catch (DataIntegrityViolationException ex) {
                errors.rejectValue("email", "duplicate", "Ese correo ya está registrado.");
            } catch (ResponseStatusException ex) {
                if (ex.getStatusCode() != HttpStatus.BAD_REQUEST) { throw ex; }
                errors.rejectValue("password", "length", ex.getReason());
            }
        }
        return "AdminUsuarioNuevo";
    }

    @GetMapping("/editar")
    String editForm(@RequestParam String email, Model model) {
        var account = accounts.find(email);
        model.addAttribute("email", account.email());
        model.addAttribute("form", new AdminAccountUpdateForm(account.name(), account.displayName(), account.role()));
        return "AdminUsuarioEditar";
    }

    @PostMapping("/editar")
    String edit(@RequestParam String email, @Valid @ModelAttribute("form") AdminAccountUpdateForm form,
            BindingResult errors, Model model) {
        model.addAttribute("email", email);
        if (!errors.hasErrors()) {
            try {
                accounts.update(email, form);
                return "redirect:/admin/usuarios?saved";
            } catch (ResponseStatusException ex) {
                if (ex.getStatusCode() != HttpStatus.BAD_REQUEST) { throw ex; }
                errors.rejectValue("role", "role", ex.getReason());
            }
        }
        return "AdminUsuarioEditar";
    }
}
