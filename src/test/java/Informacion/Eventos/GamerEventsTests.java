package Informacion.Eventos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class GamerEventsTests {
    @Autowired MockMvc mvc;
    @Autowired EventRepository events;
    @Autowired AccountService accounts;
    @Autowired JwtService jwt;
    @Autowired JdbcTemplate jdbc;
    @Autowired DataSource dataSource;

    private String bearer() {
        var account = accounts.loadUserByUsername("player2@eventos.test");
        return "Bearer " + jwt.issue(new UsernamePasswordAuthenticationToken(account, null, account.getAuthorities()));
    }

    @Test void allSevenFiltersWorkTogetherWithoutLoginAndSummariesStayPublicOnly() throws Exception {
        String date = events.find(1).orElseThrow().startsAt().toLocalDate().toString();
        for (String path : new String[]{"/eventos", "/api/eventos"}) {
            var request = get(path).param("name", "aREna").param("videoGame", "valo")
                .param("category", "Esports").param("country", "Chile").param("location", "Santiago")
                .param("date", date).param("modality", "Presencial");
            var result = mvc.perform(request).andExpect(status().isOk())
                .andExpect(content().string(containsString("Nexo Arena")))
                .andExpect(content().string(not(containsString("Av. del Encuentro"))))
                .andExpect(content().string(not(containsString("photo_order"))))
                .andExpect(content().string(not(containsString("torneo-esports.jpg"))))
                .andReturn();
            if (path.startsWith("/api")) {
                assertThat(result.getResponse().getContentAsString()).doesNotContain("registrationInfo", "requirements", "accessUrl", "description");
            } else {
                Object summaries = result.getModelAndView().getModel().get("events");
                assertThat(summaries).isInstanceOf(List.class);
                assertThat((List<?>) summaries).hasSize(1).allMatch(EventSummary.class::isInstance);
            }
        }
        mvc.perform(get("/api/eventos").param("name", "aREna").param("videoGame", "valo")
                .param("category", "Esports").param("country", "Chile").param("location", "Santiago")
                .param("date", date).param("modality", "Presencial"))
            .andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].id").value(1));
    }

    @Test void newFiltersHandleOnlineEventsLiteralTextAndEmptyValues() throws Exception {
        for (String[] filter : new String[][]{{"name", "greegame online"}, {"videoGame", "Minecraft"},
                {"country", "argentina"}, {"modality", "online"}}) {
            mvc.perform(get("/api/eventos").param(filter[0], filter[1]))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(4));
        }
        mvc.perform(get("/api/eventos").param("modality", "Online").param("country", "Chile"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        for (String value : new String[]{"%", "' OR 1=1 --", "<script>alert(1)</script>"}) {
            mvc.perform(get("/api/eventos").param("name", value))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
            mvc.perform(get("/eventos").param("name", value)).andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>alert(1)</script>"))));
        }
        mvc.perform(get("/api/eventos").param("country", " ").param("modality", " ").param("name", " "))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(4));
        mvc.perform(get("/api/eventos").param("date", "fecha-invalida")).andExpect(status().isBadRequest());
    }

    @Test void privateDetailsIncludeGameRequirementsRegistrationAndPhotographs() throws Exception {
        mvc.perform(get("/eventos/1").with(user("player2@eventos.test")))
            .andExpect(status().isOk()).andExpect(content().string(containsString("Requisitos de participación")))
            .andExpect(content().string(containsString("Información de inscripción")))
            .andExpect(content().string(containsString("torneo-esports.jpg")))
            .andExpect(content().string(containsString("Fotografías")));
        mvc.perform(get("/api/eventos/1").header("Authorization", bearer()))
            .andExpect(status().isOk()).andExpect(jsonPath("$.videoGame").value("Valorant"))
            .andExpect(jsonPath("$.requirements").isNotEmpty())
            .andExpect(jsonPath("$.registrationInfo").isNotEmpty())
            .andExpect(jsonPath("$.photos.length()").value(2));
        mvc.perform(get("/eventos/4").with(user("player2@eventos.test")))
            .andExpect(status().isOk()).andExpect(content().string(containsString("https://example.com/greegame-online")))
            .andExpect(content().string(containsString("Enlace de acceso de demostración")));
    }

    @Test void photographsRequireSessionOrJwtAndDoNotExposeArbitraryResources() throws Exception {
        mvc.perform(get("/eventos/fotos/torneo-esports.jpg")).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/api/eventos/fotos/torneo-esports.jpg")).andExpect(status().isUnauthorized());
        for (String filename : new String[]{"torneo-esports.jpg", "encuentro-retro.jpg"}) {
            mvc.perform(get("/eventos/fotos/" + filename).with(user("player2@eventos.test")))
                .andExpect(status().isOk()).andExpect(content().contentType("image/jpeg"))
                .andExpect(header().string("Cache-Control", "no-store"));
            mvc.perform(get("/api/eventos/fotos/" + filename).header("Authorization", bearer()))
                .andExpect(status().isOk()).andExpect(content().contentType("image/jpeg"));
        }
        mvc.perform(get("/api/eventos/fotos/application.properties").header("Authorization", bearer()))
            .andExpect(status().isNotFound());
        mvc.perform(get("/api/eventos/fotos/inexistente.jpg").header("Authorization", bearer()))
            .andExpect(status().isNotFound());
        mvc.perform(get("/event-photos/torneo-esports.jpg").with(user("player2@eventos.test")))
            .andExpect(status().isNotFound());
    }

    @Test void homeShowsRecentPublicationsAndPopularEventsWithoutPrivateFields() throws Exception {
        var result = mvc.perform(get("/")).andExpect(status().isOk())
            .andExpect(content().string(containsString("Publicados recientemente")))
            .andExpect(content().string(containsString("Populares en la comunidad")))
            .andExpect(content().string(not(containsString("Av. del Encuentro"))))
            .andReturn();
        @SuppressWarnings("unchecked")
        var recent = (List<EventSummary>) result.getModelAndView().getModel().get("events");
        assertThat(recent).extracting(EventSummary::id).containsExactly(4L, 3L, 2L, 1L);
        @SuppressWarnings("unchecked")
        var popular = (List<EventSummary>) result.getModelAndView().getModel().get("featured");
        assertThat(popular).extracting(EventSummary::id).containsExactly(1L, 2L);
    }

    @Test void repeatedInitializationPreservesEventsAccountsAndEditedDetails() {
        var originalEvents = jdbc.queryForList("SELECT * FROM events ORDER BY id");
        var originalAccounts = jdbc.queryForList("SELECT * FROM accounts ORDER BY email");
        String original = jdbc.queryForObject("SELECT requirements FROM event_details WHERE event_id = 1", String.class);
        try {
            jdbc.update("UPDATE event_details SET requirements = ? WHERE event_id = 1", "Requisito conservado");
            new ResourceDatabasePopulator(new ClassPathResource("data.sql")).execute(dataSource);
            assertThat(jdbc.queryForList("SELECT * FROM events ORDER BY id")).isEqualTo(originalEvents);
            assertThat(jdbc.queryForList("SELECT * FROM accounts ORDER BY email")).isEqualTo(originalAccounts);
            assertThat(jdbc.queryForObject("SELECT requirements FROM event_details WHERE event_id = 1", String.class))
                .isEqualTo("Requisito conservado");
            assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM event_photos", Integer.class)).isEqualTo(8);
        } finally {
            jdbc.update("UPDATE event_details SET requirements = ? WHERE event_id = 1", original);
        }
    }
}
