package Informacion.Eventos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminAccountService {
    private final AccountRepository accounts;
    private final AccountRoleRepository roles;
    private final PasswordEncoder encoder;
    private final EntityManager entityManager;

    public AdminAccountService(AccountRepository accounts, AccountRoleRepository roles,
            PasswordEncoder encoder, EntityManager entityManager) {
        this.accounts = accounts; this.roles = roles;
        this.encoder = encoder; this.entityManager = entityManager;
    }

    public record AccountView(String email, String name, String displayName, AccountRole.Role role) {}

    private String administrator() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        String email = authentication.getName();
        if (roles.findById(email).map(r -> r.role != AccountRole.Role.ADMIN).orElse(true)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return email;
    }

    @Transactional(readOnly = true)
    public List<AccountView> list() {
        administrator();
        Map<String, AccountRole.Role> assigned = roles.findAll().stream()
            .collect(Collectors.toMap(r -> r.email, r -> r.role));
        return accounts.findAll(Sort.by("email")).stream()
            .map(a -> view(a, assigned.getOrDefault(a.email, AccountRole.Role.USER))).toList();
    }

    @Transactional(readOnly = true)
    public AccountView find(String email) {
        administrator();
        Account account = account(email);
        return view(account, roles.findById(account.email).map(r -> r.role).orElse(AccountRole.Role.USER));
    }

    @Transactional
    public AccountView create(AdminAccountCreateForm form) {
        administrator();
        if (form.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña supera 72 bytes.");
        }
        Account account = accounts.saveAndFlush(new Account(
            new RegistrationForm(form.name(), form.email(), form.password()), encoder.encode(form.password())));
        roles.saveAndFlush(new AccountRole(account.email, form.role()));
        return view(account, form.role());
    }

    @Transactional
    public AccountView update(String email, AdminAccountUpdateForm form) {
        // Serializar cambios de roles evita que dos administradores se retiren permisos mutuamente.
        entityManager.createQuery("select r from AccountRole r order by r.email", AccountRole.class)
            .setLockMode(LockModeType.PESSIMISTIC_WRITE).getResultList();
        String actor = administrator();
        Account account = account(email);
        if (actor.equals(account.email) && form.role() != AccountRole.Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes retirar tu propio rol ADMIN.");
        }
        account.name = form.name(); account.displayName = form.displayName();
        roles.saveAndFlush(new AccountRole(account.email, form.role()));
        return view(account, form.role());
    }

    private Account account(String email) {
        return accounts.findById(email.trim().toLowerCase(Locale.ROOT))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada."));
    }

    private AccountView view(Account account, AccountRole.Role role) {
        return new AccountView(account.email, account.name, account.displayName, role);
    }
}
