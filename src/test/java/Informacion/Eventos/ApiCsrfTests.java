package Informacion.Eventos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ApiCsrfTests {
    @Autowired MockMvc mvc;
    private final String login = "{\"email\":\"player2@eventos.test\",\"password\":\"GamerDemo2026!\"}";

    @Test void apiLoginRequiresCsrfAndWorksWithCookieAndHeaderWithoutAnHttpSession() throws Exception {
        mvc.perform(post("/api/auth/login").contentType("application/json").content(login))
            .andExpect(status().isForbidden());
        var challenge = mvc.perform(get("/api/csrf")).andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", "no-store")).andReturn();
        assertThat(challenge.getRequest().getSession(false)).isNull();
        var cookie = challenge.getResponse().getCookie("XSRF-TOKEN");
        assertThat(cookie).isNotNull();
        assertThat(cookie.isHttpOnly()).isTrue();
        String body = challenge.getResponse().getContentAsString();
        String token = JsonPath.read(body, "$.token");
        String header = JsonPath.read(body, "$.headerName");
        var authenticated = mvc.perform(post("/api/auth/login").cookie(cookie).header(header, token)
                .contentType("application/json").content(login))
            .andExpect(status().isOk()).andExpect(jsonPath("$.tokenType").value("Bearer")).andReturn();
        assertThat(authenticated.getRequest().getSession(false)).isNull();
        mvc.perform(post("/api/auth/login").cookie(cookie).header(header, "invalid")
                .contentType("application/json").content(login)).andExpect(status().isForbidden());
        mvc.perform(post("/api/auth/login").header(header, token)
                .contentType("application/json").content(login)).andExpect(status().isForbidden());
    }

    @Test void aBrowserSessionCannotPublishThroughTheApiEvenWithValidCsrf() throws Exception {
        var session = mvc.perform(formLogin().user("player2@eventos.test").password("GamerDemo2026!"))
            .andExpect(status().is3xxRedirection()).andReturn().getRequest().getSession(false);
        var challenge = mvc.perform(get("/api/csrf")).andReturn().getResponse();
        String token = JsonPath.read(challenge.getContentAsString(), "$.token");
        mvc.perform(post("/api/eventos").session((MockHttpSession) session)
                .cookie(challenge.getCookie("XSRF-TOKEN")).header("X-XSRF-TOKEN", token)
                .contentType("application/json").content("{}"))
            .andExpect(status().isUnauthorized());
    }
}
