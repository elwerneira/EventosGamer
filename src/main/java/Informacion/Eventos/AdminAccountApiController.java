package Informacion.Eventos;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/usuarios")
public class AdminAccountApiController {
    private final AdminAccountService accounts;

    public AdminAccountApiController(AdminAccountService accounts) { this.accounts = accounts; }

    @GetMapping
    List<AdminAccountService.AccountView> list() { return accounts.list(); }

    @PostMapping
    ResponseEntity<AdminAccountService.AccountView> create(@Valid @RequestBody AdminAccountCreateForm form) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accounts.create(form));
    }

    @PutMapping("/{email}")
    AdminAccountService.AccountView update(@PathVariable String email,
            @Valid @RequestBody AdminAccountUpdateForm form) { return accounts.update(email, form); }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    ResponseEntity<Map<String, String>> invalid() {
        return ResponseEntity.badRequest().body(Map.of("error", "invalid_request"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, String>> conflict() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "account_conflict"));
    }
}
