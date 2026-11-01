package Informacion.Eventos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;

import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class JwtSecurityTests {
    @Autowired MockMvc mvc;
    @Autowired AccountRoleRepository roles;
    @Autowired AccountService accounts;
    @Value("${security.jwt.secret}") String secret;

    private String login(int player) throws Exception {
        var response = mvc.perform(post("/api/auth/login").contentType("application/json")
                .content("{\"email\":\"player" + player + "@eventos.test\",\"password\":\"GamerDemo2026!\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expiresIn").value(900)).andReturn();
        assertThat(response.getRequest().getSession(false)).isNull();
        return JsonPath.read(response.getResponse().getContentAsString(), "$.accessToken");
    }

    @Test void allThreeDatabaseAccountsCanUsePrivateDetails() throws Exception {
        for (int player = 1; player <= 3; player++) {
            mvc.perform(get("/api/eventos/1").header("Authorization", "Bearer " + login(player)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.address").isNotEmpty());
        }
    }

    @Test void summariesArePublicButDetailsNeedBearer() throws Exception {
        mvc.perform(get("/api/eventos")).andExpect(status().isOk())
            .andExpect(content().string(not(containsString("address"))));
        mvc.perform(get("/api/eventos/1")).andExpect(status().isUnauthorized())
            .andExpect(header().string("WWW-Authenticate", "Bearer"));
        mvc.perform(get("/api/eventos/99999").header("Authorization", "Bearer " + login(2)))
            .andExpect(status().isNotFound());
    }

    @Test void browserSessionDoesNotAuthenticateTheApi() throws Exception {
        var result = mvc.perform(formLogin().user("player1@eventos.test").password("GamerDemo2026!"))
            .andExpect(status().is3xxRedirection()).andReturn();
        mvc.perform(get("/api/eventos/1").session((MockHttpSession) result.getRequest().getSession(false)))
            .andExpect(status().isUnauthorized());
    }

    @Test void invalidExpiredWrongIssuerAndMissingExpiryTokensAreRejected() throws Exception {
        String token = login(2);
        String altered = token.substring(0, token.lastIndexOf('.') + 1) + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        for (String invalid : new String[]{"not-a-jwt", altered,
                signed("greegame", "player2@eventos.test", Instant.now().minusSeconds(5)),
                signed("another-app", "player2@eventos.test", Instant.now().plusSeconds(60)),
                signed("greegame", "missing@eventos.test", Instant.now().plusSeconds(60)),
                signed("greegame", "player2@eventos.test", null)}) {
            mvc.perform(get("/api/eventos/1").header("Authorization", "Bearer " + invalid))
                .andExpect(status().isUnauthorized());
        }
    }

    @Test void badCredentialsAndInvalidInputsDoNotIssueTokens() throws Exception {
        mvc.perform(post("/api/auth/login").contentType("application/json")
            .content("{\"email\":\"player1@eventos.test\",\"password\":\"incorrecta\"}"))
            .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType("application/json").content("{}"))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/api/admin/resumen").header("Authorization", "Bearer " + login(2)))
            .andExpect(status().isForbidden());
    }

    private String signed(String issuer, String subject, Instant expiration) {
        var builder = Jwts.builder().issuer(issuer).subject(subject);
        if (expiration != null) builder.expiration(Date.from(expiration));
        return builder.signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)), Jwts.SIG.HS256).compact();
    }

    @Test void administratorAndUserHaveDifferentAccessAndVisibleIdentity() throws Exception {
        mvc.perform(get("/api/admin/resumen").header("Authorization", "Bearer " + login(1)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.events").isNumber());
        for (int player : new int[]{2, 3}) {
            mvc.perform(get("/api/admin/resumen").header("Authorization", "Bearer " + login(player)))
                .andExpect(status().isForbidden());
        }
        var result = mvc.perform(formLogin().user("player1@eventos.test").password("GamerDemo2026!"))
            .andReturn();
        mvc.perform(get("/perfil").session((MockHttpSession) result.getRequest().getSession(false)))
            .andExpect(status().isOk()).andExpect(content().string(containsString("Administrador · GREEGAME")));
    }

    @Test void roleChangesApplyToExistingTokensWithoutTrustingTheirRoleClaim() throws Exception {
        String token = login(1);
        try {
            roles.saveAndFlush(new AccountRole("player1@eventos.test", AccountRole.Role.USER));
            accounts.run(null);
            mvc.perform(get("/api/admin/resumen").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        } finally {
            roles.saveAndFlush(new AccountRole("player1@eventos.test", AccountRole.Role.ADMIN));
        }
    }
}
