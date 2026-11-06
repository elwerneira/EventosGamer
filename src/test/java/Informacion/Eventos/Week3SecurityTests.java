package Informacion.Eventos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.*;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class Week3SecurityTests {
    @Autowired MockMvc mvc;

    @Test void webAndApiResponsesContainTheSecurityPolicies() throws Exception {
        for (String path : new String[]{"/", "/login", "/eventos", "/api/eventos", "/api/eventos/4"}) {
            mvc.perform(get(path)).andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Cross-Origin-Resource-Policy", "same-origin"))
                .andExpect(header().string("Cross-Origin-Opener-Policy", "same-origin"))
                .andExpect(header().string("Cross-Origin-Embedder-Policy", "require-corp"))
                .andExpect(header().string("Permissions-Policy", "camera=(), microphone=(), geolocation=()"))
                .andExpect(header().string("Content-Security-Policy", org.hamcrest.Matchers.containsString("script-src 'none'")));
        }
    }

    @Test void oversizedAndOutOfRangeSearchInputsReturn400() throws Exception {
        for (String path : new String[]{"/eventos", "/api/eventos"}) {
            for (String parameter : new String[]{"name", "videoGame", "category", "country", "location", "modality"}) {
                mvc.perform(get(path).param(parameter, "x".repeat(255))).andExpect(status().isBadRequest());
            }
            mvc.perform(get(path).param("date", "9999-12-31")).andExpect(status().isBadRequest());
        }
    }

    @Test void loginLimitBlocksBothPathsAndExpiresWithoutSleeping() throws Exception {
        AtomicLong time = new AtomicLong(0);
        Clock clock = new Clock() {
            public ZoneId getZone() { return ZoneOffset.UTC; }
            public Clock withZone(ZoneId zone) { return this; }
            public Instant instant() { return Instant.ofEpochMilli(time.get()); }
        };
        var limit = new LoginRateLimitFilter(2, clock);
        for (String path : new String[]{"/login", "/api/auth/login"}) {
            var request = new MockHttpServletRequest("POST", path);
            request.setRemoteAddr("192.0.2.1");
            limit.doFilter(request, new MockHttpServletResponse(), (req, res) -> {});
        }
        var blocked = new MockHttpServletResponse();
        var request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr("192.0.2.1");
        limit.doFilter(request, blocked, (req, res) -> { throw new AssertionError("Debió bloquear el login"); });
        assertThat(blocked.getStatus()).isEqualTo(429);
        assertThat(blocked.getHeader("Retry-After")).isEqualTo("60");
        assertThat(limit.allow("192.0.2.2")).isTrue();
        time.set(60_000);
        assertThat(limit.allow("192.0.2.1")).isTrue();
        var publicPage = new MockHttpServletRequest("GET", "/eventos");
        assertThat(limit.shouldNotFilter(publicPage)).isTrue();
    }

    @Test void authenticationFailuresAreLoggedWithoutPasswordsOrTokens(CapturedOutput output) throws Exception {
        mvc.perform(post("/api/auth/login").contentType("application/json")
            .content("{\"email\":\"player2@eventos.test\",\"password\":\"NoDebeAparecerEnLogs!\"}"))
            .andExpect(status().isUnauthorized());
        assertThat(output.getAll()).contains("security_event=authentication_failure")
            .doesNotContain("NoDebeAparecerEnLogs!", "Authorization: Bearer");
    }
}
