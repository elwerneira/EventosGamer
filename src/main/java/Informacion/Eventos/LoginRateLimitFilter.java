package Informacion.Eventos;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.filter.OncePerRequestFilter;

public class LoginRateLimitFilter extends OncePerRequestFilter {
    private record Window(long start, int attempts) {}
    private final Map<String, Window> windows = new HashMap<>();
    private final Clock clock;
    private final int maximum;

    LoginRateLimitFilter(int maximum) { this(maximum, Clock.systemUTC()); }

    LoginRateLimitFilter(int maximum, Clock clock) {
        if (maximum < 1) throw new IllegalArgumentException("El límite de login debe ser positivo.");
        this.maximum = maximum;
        this.clock = clock;
    }

    // shortcut: límite por IP en memoria; usar almacenamiento compartido si se despliegan varias instancias.
    synchronized boolean allow(String remoteAddress) {
        long now = clock.millis();
        windows.entrySet().removeIf(entry -> now - entry.getValue().start() >= 60_000);
        Window window = windows.get(remoteAddress);
        if (window == null && windows.size() >= 10_000) return false;
        if (window != null && window.attempts() >= maximum) return false;
        windows.put(remoteAddress, new Window(window == null ? now : window.start(),
            window == null ? 1 : window.attempts() + 1));
        return true;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equals(request.getMethod()) || !((request.getContextPath() + "/login").equals(request.getRequestURI())
            || (request.getContextPath() + "/api/auth/login").equals(request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        if (!allow(request.getRemoteAddr())) {
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"too_many_login_attempts\"}");
            return;
        }
        chain.doFilter(request, response);
    }
}
