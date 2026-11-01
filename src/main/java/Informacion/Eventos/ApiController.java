package Informacion.Eventos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final AuthenticationManager authentication;
    private final JwtService jwt;
    private final EventRepository events;
    private final AccountRepository accounts;
    private final EventJpaRepository storedEvents;

    public ApiController(AuthenticationManager authentication, JwtService jwt, EventRepository events,
            AccountRepository accounts, EventJpaRepository storedEvents) {
        this.authentication = authentication;
        this.jwt = jwt;
        this.events = events;
        this.accounts = accounts;
        this.storedEvents = storedEvents;
    }

    record Login(@NotBlank @Email @Size(max = 254) String email,
                 @NotBlank @Size(max = 72) String password) {}
    record Token(String accessToken, String tokenType, long expiresIn) {}

    // Evita registrar valores rechazados de contraseñas en el manejador predeterminado.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> invalidLogin() {
        return ResponseEntity.badRequest().body(Map.of("error", "invalid_request"));
    }

    @PostMapping("/auth/login")
    Token login(@Valid @RequestBody Login login) {
        if (login.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            var user = authentication.authenticate(new UsernamePasswordAuthenticationToken(
                login.email().trim().toLowerCase(Locale.ROOT), login.password()));
            return new Token(jwt.issue(user), "Bearer", JwtService.LIFETIME_SECONDS);
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping("/eventos")
    List<EventSummary> search(@RequestParam(required = false) String category,
            @RequestParam(required = false) LocalDate date, @RequestParam(required = false) String location,
            @RequestParam(required = false) String name, @RequestParam(required = false) String videoGame,
            @RequestParam(required = false) String country, @RequestParam(required = false) String modality) {
        return events.search(category, date, location, name, videoGame, country, modality).stream()
            .map(Event::summary).toList();
    }

    @GetMapping("/eventos/{id}")
    Event detail(@PathVariable long id) {
        return events.find(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/admin/resumen")
    Map<String, Long> adminSummary() {
        return Map.of("accounts", accounts.count(), "events", storedEvents.count());
    }
}
