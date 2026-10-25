package Informacion.Eventos;

import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountService implements UserDetailsService, ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    private final String demoPassword;

    public AccountService(JdbcTemplate jdbc, PasswordEncoder encoder,
            @Value("${EVENTOS_DEMO_PASSWORD:GamerDemo2026!}") String demoPassword) {
        this.jdbc = jdbc;
        this.encoder = encoder;
        this.demoPassword = demoPassword;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        return jdbc.query("SELECT email, password_hash FROM accounts WHERE email = ?",
                (rs, row) -> User.withUsername(rs.getString("email"))
                    .password(rs.getString("password_hash")).roles("USER").build(),
                email.trim().toLowerCase(Locale.ROOT)).stream().findFirst()
            .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas."));
    }

    public void register(RegistrationForm form) {
        jdbc.update("INSERT INTO accounts (email, name, password_hash, display_name) VALUES (?, ?, ?, ?)",
                form.email(), form.name(), encoder.encode(form.password()),
                form.name().substring(0, Math.min(40, form.name().length())));
    }

    public ProfileForm profile(String email) {
        return jdbc.queryForObject("SELECT display_name, games, avatar, notifications FROM accounts WHERE email = ?",
                (rs, row) -> new ProfileForm(rs.getString("display_name"), rs.getString("games"),
                    rs.getString("avatar"), rs.getBoolean("notifications")), email);
    }

    public void updateProfile(String email, ProfileForm form) {
        jdbc.update("UPDATE accounts SET display_name = ?, games = ?, avatar = ?, notifications = ? WHERE email = ?",
                form.displayName(), form.games(), form.avatar(), form.notifications(), email);
    }

    @Override
    public void run(ApplicationArguments args) {
        for (int i = 1; i <= 3; i++) {
            String email = "player" + i + "@eventos.test";
            if (jdbc.queryForObject("SELECT COUNT(*) FROM accounts WHERE email = ?", Integer.class, email) == 0) {
                register(new RegistrationForm("Jugador " + i, email, demoPassword));
            }
        }
    }
}
