package Informacion.Eventos;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService implements UserDetailsService, ApplicationRunner {
    private final AccountRepository accounts;
    private final PasswordEncoder encoder;
    private final String demoPassword;
    private final AccountRoleRepository roles;

    public AccountService(AccountRepository accounts, PasswordEncoder encoder, AccountRoleRepository roles,
            @Value("${EVENTOS_DEMO_PASSWORD:GamerDemo2026!}") String demoPassword) {
        this.accounts = accounts;
        this.encoder = encoder;
        this.demoPassword = demoPassword;
        this.roles = roles;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return accounts.findById(email.trim().toLowerCase(Locale.ROOT))
            .map(account -> User.withUsername(account.email).password(account.passwordHash)
                .roles(roles.findById(account.email).map(authority -> authority.role.name()).orElse("USER")).build())
            .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas."));
    }

    @Transactional
    public void register(RegistrationForm form) {
        accounts.saveAndFlush(new Account(form, encoder.encode(form.password())));
        roles.save(new AccountRole(form.email(), AccountRole.Role.USER));
    }

    public ProfileForm profile(String email) {
        Account account = accounts.findById(email)
            .orElseThrow(() -> new UsernameNotFoundException("Cuenta no encontrada."));
        return new ProfileForm(account.displayName, account.games, account.avatar, account.notifications);
    }

    @Transactional
    public void updateProfile(String email, ProfileForm form) {
        Account account = accounts.findById(email)
            .orElseThrow(() -> new UsernameNotFoundException("Cuenta no encontrada."));
        account.displayName = form.displayName();
        account.games = form.games();
        account.avatar = form.avatar();
        account.notifications = form.notifications();
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (int i = 1; i <= 3; i++) {
            String email = "player" + i + "@eventos.test";
            boolean newAccount = !accounts.existsById(email);
            if (newAccount) {
                register(new RegistrationForm("Jugador " + i, email, demoPassword));
            }
            if (newAccount || !roles.existsById(email)) {
                roles.save(new AccountRole(email, i == 1 ? AccountRole.Role.ADMIN : AccountRole.Role.USER));
            }
        }
    }
}
