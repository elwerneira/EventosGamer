package Informacion.Eventos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class EventosSecurityTests {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder encoder;
    @Autowired EventRepository events;

    @Test
    void publicPagesAndCssAreAccessible() throws Exception {
        for (String path : new String[]{"/", "/home", "/login", "/registro", "/login.css"}) {
            mvc.perform(get(path)).andExpect(status().isOk());
        }
        mvc.perform(get("/")).andExpect(content().string(containsString("En el radar")))
            .andExpect(content().string(containsString("/registro")))
            .andExpect(content().string(not(containsString("Cerrar sesión"))))
            .andExpect(content().string(not(containsString("Av. del Encuentro"))));
    }

    @Test
    void retiredUrlDoesNotServeHome() throws Exception {
        mvc.perform(get("/recetas")).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/recetas").with(user("player1@eventos.test"))).andExpect(status().isNotFound());
    }

    @Test
    void anonymousUsersCannotAccessPrivatePagesOrUnlistedRoutes() throws Exception {
        for (String path : new String[]{"/perfil", "/eventos", "/eventos/1", "/schema.sql", "/data/eventos.mv.db"}) {
            mvc.perform(get(path)).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        }
    }

    @Test
    void allThreeDemoUsersCanLogInAndPasswordsAreHashed() throws Exception {
        for (int i = 1; i <= 3; i++) {
            String email = "player" + i + "@eventos.test";
            mvc.perform(formLogin("/login").user(email).password("GamerDemo2026!"))
                .andExpect(authenticated().withUsername(email)).andExpect(redirectedUrl("/eventos"));
            String hash = jdbc.queryForObject("SELECT password_hash FROM accounts WHERE email = ?", String.class, email);
            assertThat(hash).startsWith("$2").isNotEqualTo("GamerDemo2026!");
            assertThat(encoder.matches("GamerDemo2026!", hash)).isTrue();
        }
    }

    @Test
    void invalidLoginIsRejected() throws Exception {
        mvc.perform(formLogin("/login").user("player1@eventos.test").password("incorrecta"))
            .andExpect(unauthenticated()).andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void registrationCreatesAnAccountThatCanAuthenticate() throws Exception {
        mvc.perform(post("/registro").with(csrf()).param("name", "Nueva Jugadora")
                .param("email", "NUEVA@eventos.test").param("password", "NuevaClave2026!"))
            .andExpect(redirectedUrl("/login?registered"));
        var result = mvc.perform(formLogin("/login").user("nueva@eventos.test").password("NuevaClave2026!"))
            .andExpect(authenticated()).andReturn();
        mvc.perform(get("/perfil").session((MockHttpSession) result.getRequest().getSession(false)))
            .andExpect(status().isOk()).andExpect(content().string(containsString("Nueva Jugadora")));
    }

    @Test
    void duplicateEmailCannotOverwriteAnAccount() throws Exception {
        String before = jdbc.queryForObject("SELECT password_hash FROM accounts WHERE email = ?", String.class,
                "player1@eventos.test");
        mvc.perform(post("/registro").with(csrf()).param("name", "Duplicado")
                .param("email", "PLAYER1@eventos.test").param("password", "OtraClave2026!"))
            .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("form", "email"))
            .andExpect(content().string(containsString("No se pudo registrar")));
        assertThat(jdbc.queryForObject("SELECT password_hash FROM accounts WHERE email = ?", String.class,
                "player1@eventos.test")).isEqualTo(before);
    }

    @Test
    void invalidRegistrationDoesNotWriteToDatabaseOrEchoPassword() throws Exception {
        int before = jdbc.queryForObject("SELECT COUNT(*) FROM accounts", Integer.class);
        mvc.perform(post("/registro").with(csrf()).param("name", " ")
                .param("email", "no-es-correo").param("password", "corta123"))
            .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("form", "name", "email", "password"))
            .andExpect(content().string(not(containsString("value=\"corta123\""))));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM accounts", Integer.class)).isEqualTo(before);
    }

    @Test
    void multibytePasswordOverBcryptLimitIsRejected() throws Exception {
        mvc.perform(post("/registro").with(csrf()).param("name", "Jugadora")
                .param("email", "bytes@eventos.test").param("password", "界".repeat(30)))
            .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("form", "password"));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM accounts WHERE email = ?", Integer.class,
                "bytes@eventos.test")).isZero();
    }

    @Test
    void csrfProtectsRegistrationLoginProfileAndLogout() throws Exception {
        mvc.perform(post("/registro")).andExpect(status().isForbidden());
        mvc.perform(post("/login")).andExpect(status().isForbidden());
        mvc.perform(post("/perfil").with(user("player1@eventos.test"))).andExpect(status().isForbidden());
        mvc.perform(post("/logout").with(user("player1@eventos.test"))).andExpect(status().isForbidden());
    }

    @Test
    void profileIsSavedForTheAuthenticatedUserOnlyAndEscapesHtml() throws Exception {
        String otherBefore = jdbc.queryForObject("SELECT display_name FROM accounts WHERE email = ?", String.class,
                "player2@eventos.test");
        mvc.perform(post("/perfil").with(user("player1@eventos.test")).with(csrf())
                .param("displayName", "<script>alert(1)</script>").param("games", "Valorant, Minecraft")
                .param("avatar", "mago").param("notifications", "true").param("email", "player2@eventos.test"))
            .andExpect(redirectedUrl("/perfil?saved"));
        assertThat(jdbc.queryForObject("SELECT display_name FROM accounts WHERE email = ?", String.class,
                "player2@eventos.test")).isEqualTo(otherBefore);
        assertThat(jdbc.queryForObject("SELECT notifications FROM accounts WHERE email = ?", Boolean.class,
                "player1@eventos.test")).isTrue();
        mvc.perform(get("/perfil").with(user("player1@eventos.test")))
            .andExpect(status().isOk()).andExpect(content().string(containsString("&lt;script&gt;")))
            .andExpect(content().string(not(containsString("<script>alert(1)</script>"))));
    }

    @Test
    void invalidAvatarAndEmptyGamesCannotOverwriteTheProfile() throws Exception {
        mvc.perform(post("/perfil").with(user("player3@eventos.test")).with(csrf())
                .param("displayName", "Jugador").param("games", " ").param("avatar", "https://otro.test/avatar"))
            .andExpect(status().isOk()).andExpect(model().attributeHasFieldErrors("form", "games", "avatar"));
        assertThat(jdbc.queryForObject("SELECT avatar FROM accounts WHERE email = ?", String.class,
                "player3@eventos.test")).isEqualTo("explorador");
    }

    @Test
    void malformedCheckboxReturns400InsteadOfRenderingAnInvalidProfile() throws Exception {
        mvc.perform(post("/perfil").with(user("player3@eventos.test")).with(csrf())
                .param("displayName", "Jugador").param("games", "Minecraft").param("avatar", "mago")
                .param("notifications", "valor-invalido"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void filtersWorkTogetherAndShowAnEmptyState() throws Exception {
        String date = events.find(1).orElseThrow().startsAt().toLocalDate().toString();
        mvc.perform(get("/eventos").with(user("player1@eventos.test"))
                .param("category", "Esports").param("location", "Santiago").param("date", date))
            .andExpect(status().isOk()).andExpect(content().string(containsString("Nexo Arena")))
            .andExpect(content().string(not(containsString("Pixel Independiente"))));
        mvc.perform(get("/eventos").with(user("player1@eventos.test")).param("location", "Sin coincidencias"))
            .andExpect(status().isOk()).andExpect(content().string(containsString("No hay coincidencias")));
    }

    @Test
    void malformedDateAndSqlInjectionDoNotExposeEvents() throws Exception {
        mvc.perform(get("/eventos").with(user("player1@eventos.test")).param("date", "fecha-mala"))
            .andExpect(status().isBadRequest());
        mvc.perform(get("/eventos").with(user("player1@eventos.test")).param("category", "' OR 1=1 --"))
            .andExpect(status().isOk()).andExpect(content().string(containsString("No hay coincidencias")));
    }

    @Test
    void detailsIncludeAllRequiredInformationAndUnknownEventsReturn404() throws Exception {
        mvc.perform(get("/eventos/1").with(user("player1@eventos.test")))
            .andExpect(status().isOk()).andExpect(content().string(containsString("Av. del Encuentro")))
            .andExpect(content().string(containsString("Fecha y hora")))
            .andExpect(content().string(containsString("Organizadores")))
            .andExpect(content().string(containsString("Principales expositores")))
            .andExpect(content().string(containsString("Estacionamiento")))
            .andExpect(content().string(containsString("Locomoción colectiva")))
            .andExpect(content().string(containsString("Comidas")))
            .andExpect(content().string(containsString("Alojamiento")));
        mvc.perform(get("/eventos/999999").with(user("player1@eventos.test"))).andExpect(status().isNotFound());
    }

    @Test
    void logoutInvalidatesTheSessionAndPrivateAccess() throws Exception {
        var result = mvc.perform(formLogin("/login").user("player3@eventos.test").password("GamerDemo2026!"))
            .andExpect(authenticated()).andReturn();
        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        mvc.perform(post("/logout").session(session).with(csrf()))
            .andExpect(redirectedUrl("/login?logout")).andExpect(unauthenticated());
        assertThat(session.isInvalid()).isTrue();
        mvc.perform(get("/perfil")).andExpect(redirectedUrl("/login"));
    }
}
