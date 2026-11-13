package Informacion.Eventos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventPublicationTests {
    @Autowired MockMvc mvc;
    @Autowired EventJpaRepository events;
    @Autowired AccountService accounts;
    @Autowired JwtService jwt;

    private String bearer() {
        var account = accounts.loadUserByUsername("player2@eventos.test");
        return "Bearer " + jwt.issue(new UsernamePasswordAuthenticationToken(account, null, account.getAuthorities()));
    }

    private String payload() {
        return """
            {"title":"Encuentro gamer S4","category":"Comunidad","location":"Santiago",
            "startsAt":"2030-06-01T18:00:00","description":"Encuentro para jugar juntos.",
            "videoGame":"Minecraft","country":"Chile","modality":"Online",
            "address":"No aplica: online","organizers":"Comunidad de prueba",
            "accessUrl":"https://example.com/evento","requirements":"Cuenta de juego",
            "registrationInfo":"Inscripción previa con la organización"}
            """;
    }

    @Test void publicationRequiresAuthenticationAndWebCsrf() throws Exception {
        mvc.perform(get("/eventos/publicar")).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/eventos/publicar").with(user("player2@eventos.test")))
            .andExpect(status().isOk()).andExpect(content().string(containsString("name=\"_csrf\"")));
        mvc.perform(post("/eventos/publicar").with(user("player2@eventos.test")))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/eventos").with(csrf().asHeader()).contentType("application/json").content(payload()))
            .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/eventos").header("Authorization", bearer()).contentType("application/json").content(payload()))
            .andExpect(status().isForbidden());
    }

    @Test void userCanPublishAndReadPersistedEventWithoutReplacingExistingEvents() throws Exception {
        var original = events.findById(1L).orElseThrow().title;
        long before = events.count();
        String response = mvc.perform(post("/api/eventos").with(csrf().asHeader())
                .header("Authorization", bearer()).contentType("application/json").content(payload()))
            .andExpect(status().isCreated()).andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.videoGame").value("Minecraft")).andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(response, "$.id")).longValue();
        assertThat(id).isGreaterThan(4);
        assertThat(events.count()).isEqualTo(before + 1);
        assertThat(events.findById(1L).orElseThrow().title).isEqualTo(original);
        mvc.perform(get("/api/eventos/" + id).header("Authorization", bearer()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.registrationInfo").value("Inscripción previa con la organización"));
        mvc.perform(get("/api/eventos").param("name", "Encuentro gamer S4"))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));
    }

    @Test void invalidPublicationDoesNotWriteToDatabase() throws Exception {
        long before = events.count();
        for (String invalid : new String[]{payload().replace("2030-06-01", "2020-06-01"),
                payload().replace("2030-06-01", "2200-06-01"), payload().replace("Online", "Invalid"),
                payload().replace("https://example.com/evento", "javascript:alert(1)"),
                payload().replace("https://example.com/evento", ""),
                payload().replace("Encuentro gamer S4", "x".repeat(121))}) {
            mvc.perform(post("/api/eventos").with(csrf().asHeader()).header("Authorization", bearer())
                    .contentType("application/json").content(invalid))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("invalid_event"));
        }
        assertThat(events.count()).isEqualTo(before);
    }

    @Test void webPublicationEscapesUserSuppliedContent() throws Exception {
        var response = mvc.perform(post("/eventos/publicar").with(user("player2@eventos.test")).with(csrf())
                .param("title", "<script>alert(1)</script>").param("category", "Comunidad")
                .param("location", "Santiago").param("startsAt", "2030-06-01T18:00:00")
                .param("description", "Encuentro de prueba").param("videoGame", "Minecraft")
                .param("country", "Chile").param("modality", "Presencial")
                .param("address", "Dirección de ejemplo").param("organizers", "Organización")
                .param("accessUrl", "").param("requirements", "Consultar")
                .param("registrationInfo", "Consultar"))
            .andExpect(status().is3xxRedirection()).andReturn().getResponse();
        String html = mvc.perform(get(response.getRedirectedUrl()).with(user("player2@eventos.test")))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(html).contains("&lt;script&gt;").doesNotContain("<script>alert(1)</script>");
    }

    @Test void invalidWebFormShowsErrorsWithoutCreatingAnEvent() throws Exception {
        long before = events.count();
        mvc.perform(post("/eventos/publicar").with(csrf()).with(user("player2@eventos.test")))
            .andExpect(status().isOk()).andExpect(content().string(containsString("Revisa los datos del evento")));
        assertThat(events.count()).isEqualTo(before);
    }
}
