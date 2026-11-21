package Informacion.Eventos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
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
class Week5CommunityTests {
    @Autowired MockMvc mvc;
    @Autowired AccountService accounts;
    @Autowired JwtService jwt;
    @Autowired EventOwnershipRepository owners;
    @Autowired EventOwnershipBootstrap bootstrap;
    @Autowired EventCommentRepository comments;
    @Autowired EventRatingRepository ratings;
    @Autowired EventShareRepository shares;

    private String bearer(int player) {
        var account = accounts.loadUserByUsername("player" + player + "@eventos.test");
        return "Bearer " + jwt.issue(new UsernamePasswordAuthenticationToken(account, null, account.getAuthorities()));
    }

    private long publish() throws Exception {
        String body = """
            {"title":"Evento con autor S5","category":"Comunidad","location":"Santiago",
            "startsAt":"2030-06-01T18:00:00","description":"Prueba de publicación",
            "videoGame":"Minecraft","country":"Chile","modality":"Presencial",
            "address":"Dirección ficticia","organizers":"Comunidad",
            "accessUrl":"","requirements":"Consultar","registrationInfo":"Consultar"}
            """;
        String response = mvc.perform(post("/api/eventos").with(csrf().asHeader())
                .header("Authorization", bearer(2)).contentType("application/json").content(body))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    @Test void publicationStoresAuthenticatedAuthorAndMigrationPreservesOwners() throws Exception {
        long id = publish();
        assertThat(owners.findById(id).orElseThrow().authorEmail).isEqualTo("player2@eventos.test");
        assertThat(owners.findById(1L).orElseThrow().authorEmail).isEqualTo("player1@eventos.test");
        bootstrap.run(null);
        assertThat(owners.findById(id).orElseThrow().authorEmail).isEqualTo("player2@eventos.test");
        mvc.perform(get("/api/mis-eventos").header("Authorization", bearer(2)))
            .andExpect(status().isOk()).andExpect(jsonPath("$[*].id", hasItem((int) id)))
            .andExpect(jsonPath("$[*].id", not(hasItem(1))));
    }

    @Test void webListsRenderPublishedAndSharedEvents() throws Exception {
        long id = publish();
        mvc.perform(get("/mis-eventos").with(user("player2@eventos.test")))
            .andExpect(status().isOk()).andExpect(content().string(containsString("Evento con autor S5")));
        mvc.perform(post("/api/eventos/" + id + "/compartir").with(csrf().asHeader())
            .header("Authorization", bearer(2))).andExpect(status().isOk());
        mvc.perform(get("/comunidad").with(user("player3@eventos.test")))
            .andExpect(status().isOk()).andExpect(content().string(containsString("Evento con autor S5")));
    }

    @Test void allCommunityReadsArePrivate() throws Exception {
        for (String path : new String[]{"/api/mis-eventos", "/api/comunidad", "/api/eventos/1/comunidad"}) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized());
        }
        for (String path : new String[]{"/mis-eventos", "/comunidad", "/eventos/1/comunidad"}) {
            mvc.perform(get(path)).andExpect(redirectedUrl("/login"));
        }
    }

    @Test void commentRatingAndShareRequireJwtAndCsrf() throws Exception {
        for (var request : new org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder[]{
                post("/api/eventos/1/comentarios").content("{\"body\":\"Hola\"}"),
                put("/api/eventos/1/valoracion").content("{\"score\":5}"), post("/api/eventos/1/compartir")}) {
            mvc.perform(request.with(csrf().asHeader()).contentType("application/json"))
                .andExpect(status().isUnauthorized());
        }
        mvc.perform(post("/api/eventos/1/comentarios").header("Authorization", bearer(2))
            .contentType("application/json").content("{\"body\":\"Hola\"}"))
            .andExpect(status().isForbidden());
        mvc.perform(put("/api/eventos/1/valoracion").header("Authorization", bearer(2))
            .contentType("application/json").content("{\"score\":5}"))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/eventos/1/compartir").header("Authorization", bearer(1)))
            .andExpect(status().isForbidden());
    }

    @Test void ownerCanShareAndRepeatedSharingDoesNotDuplicateTheFeed() throws Exception {
        long id = publish();
        long before = shares.count();
        for (int i = 0; i < 2; i++) {
            mvc.perform(post("/api/eventos/" + id + "/compartir").with(csrf().asHeader())
                    .header("Authorization", bearer(2)).header("Host", "host-ajeno.test"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.url").value("http://localhost:8080/eventos/" + id))
                .andExpect(jsonPath("$.whatsapp", startsWith("https://wa.me/")))
                .andExpect(jsonPath("$.facebook", startsWith("https://www.facebook.com/")))
                .andExpect(jsonPath("$.x", startsWith("https://twitter.com/")));
        }
        assertThat(shares.count()).isEqualTo(before + 1);
        mvc.perform(get("/api/comunidad").header("Authorization", bearer(3)))
            .andExpect(status().isOk()).andExpect(jsonPath("$[*].id", hasItem((int) id)));
    }

    @Test void anotherUserAndAdministratorCannotShareAnEventTheyDoNotOwn() throws Exception {
        long id = publish();
        for (int player : new int[]{1, 3}) {
            mvc.perform(post("/api/eventos/" + id + "/compartir").with(csrf().asHeader())
                    .header("Authorization", bearer(player)))
                .andExpect(status().isForbidden());
        }
        assertThat(shares.existsById(id)).isFalse();
    }

    @Test void commentsPersistWithCurrentAuthorAndAreEscapedInHtml() throws Exception {
        long before = comments.countByEventId(1);
        mvc.perform(post("/api/eventos/1/comentarios").with(csrf().asHeader()).header("Authorization", bearer(2))
                .contentType("application/json").content("{\"body\":\"<script>alert(1)</script>\"}"))
            .andExpect(status().isCreated());
        assertThat(comments.countByEventId(1)).isEqualTo(before + 1);
        String json = mvc.perform(get("/api/eventos/1/comunidad").header("Authorization", bearer(3)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.comments[0].author").value(accounts.profile("player2@eventos.test").displayName()))
            .andReturn().getResponse().getContentAsString();
        assertThat(json).doesNotContain("@eventos.test", "passwordHash");
        mvc.perform(get("/eventos/1/comunidad").with(user("player3@eventos.test")))
            .andExpect(status().isOk()).andExpect(content().string(containsString("&lt;script&gt;alert(1)&lt;/script&gt;")));
    }

    @Test void invalidCommentsAndScoresDoNotWriteToTheDatabase() throws Exception {
        long before = comments.countByEventId(1), votes = ratings.countByEventId(1);
        for (String body : new String[]{"", "   ", "x".repeat(501)}) {
            mvc.perform(post("/api/eventos/1/comentarios").with(csrf().asHeader()).header("Authorization", bearer(2))
                .contentType("application/json").content("{\"body\":\"" + body + "\"}"))
                .andExpect(status().isBadRequest());
        }
        for (String score : new String[]{"0", "6", "null"}) {
            mvc.perform(put("/api/eventos/1/valoracion").with(csrf().asHeader()).header("Authorization", bearer(2))
                .contentType("application/json").content("{\"score\":" + score + "}"))
                .andExpect(status().isBadRequest());
        }
        assertThat(comments.countByEventId(1)).isEqualTo(before);
        assertThat(ratings.countByEventId(1)).isEqualTo(votes);
    }

    @Test void aUserCanUpdateOneRatingAndTheAverageUsesDistinctUsers() throws Exception {
        for (int score : new int[]{2, 5}) {
            mvc.perform(put("/api/eventos/1/valoracion").with(csrf().asHeader()).header("Authorization", bearer(2))
                .contentType("application/json").content("{\"score\":" + score + "}"))
                .andExpect(status().isNoContent());
        }
        assertThat(ratings.countByEventId(1)).isEqualTo(1);
        mvc.perform(put("/api/eventos/1/valoracion").with(csrf().asHeader()).header("Authorization", bearer(3))
            .contentType("application/json").content("{\"score\":1}"))
            .andExpect(status().isNoContent());
        mvc.perform(get("/api/eventos/1/comunidad").header("Authorization", bearer(2)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.votes").value(2))
            .andExpect(jsonPath("$.average").value(3.0)).andExpect(jsonPath("$.myRating").value(5));
    }

    @Test void webFormsKeepCsrfAndOnlyOwnerSeesManagementControls() throws Exception {
        long id = publish();
        mvc.perform(get("/eventos/" + id + "/comunidad").with(user("player2@eventos.test")))
            .andExpect(status().isOk()).andExpect(content().string(containsString("name=\"file\"")))
            .andExpect(content().string(containsString("name=\"_csrf\"")));
        mvc.perform(get("/eventos/" + id + "/comunidad").with(user("player3@eventos.test")))
            .andExpect(status().isOk()).andExpect(content().string(not(containsString("name=\"file\""))));
        mvc.perform(post("/eventos/1/comentarios").with(user("player2@eventos.test")).param("body", "Hola"))
            .andExpect(status().isForbidden());
        mvc.perform(post("/eventos/1/comentarios").with(user("player2@eventos.test")).with(csrf()).param("body", " "))
            .andExpect(redirectedUrl("/eventos/1/comunidad")).andExpect(flash().attributeExists("error"));
        mvc.perform(post("/eventos/1/valoracion").with(user("player2@eventos.test")).with(csrf()).param("score", "9"))
            .andExpect(redirectedUrl("/eventos/1/comunidad")).andExpect(flash().attributeExists("error"));
    }

    @Test void communityAndActionsReturn404ForMissingEvents() throws Exception {
        mvc.perform(get("/api/eventos/999999/comunidad").header("Authorization", bearer(2))).andExpect(status().isNotFound());
        mvc.perform(post("/api/eventos/999999/comentarios").with(csrf().asHeader()).header("Authorization", bearer(2))
            .contentType("application/json").content("{\"body\":\"Hola\"}"))
            .andExpect(status().isNotFound());
        mvc.perform(put("/api/eventos/999999/valoracion").with(csrf().asHeader()).header("Authorization", bearer(2))
            .contentType("application/json").content("{\"score\":3}"))
            .andExpect(status().isNotFound());
        mvc.perform(post("/api/eventos/999999/compartir").with(csrf().asHeader()).header("Authorization", bearer(2)))
            .andExpect(status().isNotFound());
    }
}
