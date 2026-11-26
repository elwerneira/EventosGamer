package Informacion.Eventos;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class Week6AdministrationTests {
    @Autowired MockMvc mvc;
    @Autowired AccountService authentication;
    @Autowired JwtService jwt;
    @Autowired AccountRepository accounts;
    @Autowired AccountRoleRepository roles;
    @Autowired PasswordEncoder encoder;

    private String bearer(int player) {
        var account=authentication.loadUserByUsername("player"+player+"@eventos.test");
        return "Bearer "+jwt.issue(new UsernamePasswordAuthenticationToken(account,null,account.getAuthorities()));
    }

    @Test void onlyAdministratorsCanReadManagementPagesAndApi() throws Exception {
        for (String path:new String[]{"/admin/usuarios","/admin/usuarios/nuevo","/admin/usuarios/editar?email=player2@eventos.test"}) {
            mvc.perform(get(path)).andExpect(redirectedUrl("/login"));
            mvc.perform(get(path).with(user("player2@eventos.test"))).andExpect(status().isForbidden());
            mvc.perform(get(path).with(user("player1@eventos.test").roles("ADMIN")))
                .andExpect(status().isOk()).andExpect(content().string(containsString("GREEGAME")));
        }
        mvc.perform(get("/api/admin/usuarios")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/usuarios").header("Authorization",bearer(2))).andExpect(status().isForbidden());
        mvc.perform(get("/api/admin/usuarios").header("Authorization",bearer(1)))
            .andExpect(status().isOk()).andExpect(jsonPath("$[*].role",hasItem("ADMIN")))
            .andExpect(content().string(not(containsString("passwordHash"))))
            .andExpect(content().string(not(containsString("$2a$"))));
    }

    @Test void administratorCreatesAccountsWithHashedPasswordAndRequestedRole() throws Exception {
        mvc.perform(post("/api/admin/usuarios").with(csrf().asHeader()).header("Authorization",bearer(1))
            .contentType("application/json").content("""
            {"name":"Nueva administradora","email":" NEW-ADMIN@EXAMPLE.TEST ","password":"StrongDemo123!","role":"ADMIN"}
            """))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.email").value("new-admin@example.test"))
            .andExpect(jsonPath("$.role").value("ADMIN")).andExpect(jsonPath("$.password").doesNotExist());
        var account=accounts.findById("new-admin@example.test").orElseThrow();
        assertThat(account.passwordHash).isNotEqualTo("StrongDemo123!");
        assertThat(encoder.matches("StrongDemo123!",account.passwordHash)).isTrue();
        assertThat(authentication.loadUserByUsername(account.email).getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
    }

    @Test void userCannotCreateAccountsAndAdminWritesRequireCsrf() throws Exception {
        String body="""
            {"name":"Cuenta","email":"forbidden@example.test","password":"StrongDemo123!","role":"ADMIN"}
            """;
        mvc.perform(post("/api/admin/usuarios").with(csrf().asHeader()).header("Authorization",bearer(2))
            .contentType("application/json").content(body)).andExpect(status().isForbidden());
        mvc.perform(post("/api/admin/usuarios").header("Authorization",bearer(1))
            .contentType("application/json").content(body)).andExpect(status().isForbidden());
        assertThat(accounts.existsById("forbidden@example.test")).isFalse();
    }

    @Test void profileAndRoleEditsPreservePasswordAndIdentity() throws Exception {
        String before=accounts.findById("player2@eventos.test").orElseThrow().passwordHash;
        mvc.perform(put("/api/admin/usuarios/player2@eventos.test").with(csrf().asHeader())
            .header("Authorization",bearer(1)).contentType("application/json")
            .content("""
            {"name":"Jugador dos","displayName":"Administrador dos","role":"ADMIN"}
            """))
            .andExpect(status().isOk()).andExpect(jsonPath("$.role").value("ADMIN"));
        assertThat(accounts.findById("player2@eventos.test").orElseThrow().passwordHash).isEqualTo(before);
        assertThat(accounts.findById("player2@eventos.test").orElseThrow().displayName).isEqualTo("Administrador dos");
    }

    @Test void administratorCannotRemoveOwnAdministratorRole() throws Exception {
        mvc.perform(put("/api/admin/usuarios/player1@eventos.test").with(csrf().asHeader())
            .header("Authorization",bearer(1)).contentType("application/json")
            .content("""
            {"name":"Jugador uno","displayName":"Jugador uno","role":"USER"}
            """))
            .andExpect(status().isBadRequest());
        assertThat(roles.findById("player1@eventos.test").orElseThrow().role).isEqualTo(AccountRole.Role.ADMIN);
    }

    @Test void staleJwtAndWebSessionCannotKeepRevokedAdministratorAccess() throws Exception {
        roles.saveAndFlush(new AccountRole("player2@eventos.test",AccountRole.Role.ADMIN));
        String oldToken=bearer(2);
        roles.saveAndFlush(new AccountRole("player2@eventos.test",AccountRole.Role.USER));
        mvc.perform(get("/api/admin/usuarios").header("Authorization",oldToken)).andExpect(status().isForbidden());
        mvc.perform(get("/admin/usuarios").with(user("player2@eventos.test").roles("ADMIN")))
            .andExpect(status().isForbidden());
    }

    @Test void invalidJsonAndMissingAccountsHaveExplicitClientErrors() throws Exception {
        for(String body:new String[]{"{}","{\"role\":\"SUPERADMIN\"}",
                "{\"name\":\"X\",\"email\":\"bad\",\"password\":\"short\",\"role\":\"USER\"}"}) {
            mvc.perform(post("/api/admin/usuarios").with(csrf().asHeader()).header("Authorization",bearer(1))
                .contentType("application/json").content(body)).andExpect(status().isBadRequest());
        }
        mvc.perform(put("/api/admin/usuarios/missing@example.test").with(csrf().asHeader())
            .header("Authorization",bearer(1)).contentType("application/json")
            .content("{\"name\":\"X\",\"displayName\":\"X\",\"role\":\"USER\"}"))
            .andExpect(status().isNotFound());
    }

    @Test void duplicateEmailCannotOverwriteExistingAccounts() throws Exception {
        String before=accounts.findById("player2@eventos.test").orElseThrow().passwordHash;
        mvc.perform(post("/api/admin/usuarios").with(csrf().asHeader()).header("Authorization",bearer(1))
            .contentType("application/json").content("""
            {"name":"Otro","email":"player2@eventos.test","password":"AnotherPass123!","role":"ADMIN"}
            """))
            .andExpect(status().isConflict());
        assertThat(before).isNotBlank();
    }

    @Test void frontendCreatesAndEditsWithoutEchoingPasswords() throws Exception {
        mvc.perform(post("/admin/usuarios/nuevo").with(user("player1@eventos.test").roles("ADMIN")).with(csrf())
            .param("name","Nueva cuenta").param("email","web-account@example.test")
            .param("password","StrongDemo123!").param("role","USER"))
            .andExpect(redirectedUrl("/admin/usuarios?created"));
        mvc.perform(post("/admin/usuarios/editar").with(user("player1@eventos.test").roles("ADMIN")).with(csrf())
            .param("email","web-account@example.test").param("name","Nombre actualizado")
            .param("displayName","Gamer").param("role","ADMIN"))
            .andExpect(redirectedUrl("/admin/usuarios?saved"));
        mvc.perform(post("/admin/usuarios/nuevo").with(user("player1@eventos.test").roles("ADMIN")).with(csrf())
            .param("name","").param("email","bad").param("password","SecretThatMustNotBeEchoed").param("role","USER"))
            .andExpect(view().name("AdminUsuarioNuevo"))
            .andExpect(content().string(not(containsString("SecretThatMustNotBeEchoed"))));
    }

    @Test void frontendRendersValidationAndSelfDemotionErrors() throws Exception {
        mvc.perform(post("/admin/usuarios/editar").with(user("player1@eventos.test").roles("ADMIN")).with(csrf())
            .param("email","player1@eventos.test").param("name","Jugador uno")
            .param("displayName","Jugador uno").param("role","USER"))
            .andExpect(view().name("AdminUsuarioEditar")).andExpect(model().attributeHasFieldErrors("form","role"));
        mvc.perform(post("/admin/usuarios/editar").with(user("player1@eventos.test").roles("ADMIN")).with(csrf())
            .param("email","player2@eventos.test").param("name","").param("displayName","X").param("role","USER"))
            .andExpect(view().name("AdminUsuarioEditar")).andExpect(model().attributeHasFieldErrors("form","name"));
    }

    @Test void bcryptByteLimitIsCheckedInApiAndForm() throws Exception {
        String password="é".repeat(40);
        mvc.perform(post("/api/admin/usuarios").with(csrf().asHeader()).header("Authorization",bearer(1))
            .contentType("application/json").content("{\"name\":\"X\",\"email\":\"utf8@example.test\",\"password\":\""+password+"\",\"role\":\"USER\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/admin/usuarios/nuevo").with(user("player1@eventos.test").roles("ADMIN")).with(csrf())
            .param("name","X").param("email","utf8@example.test").param("password",password).param("role","USER"))
            .andExpect(view().name("AdminUsuarioNuevo")).andExpect(model().attributeHasFieldErrors("form","password"));
        assertThat(accounts.existsById("utf8@example.test")).isFalse();
    }

    @Test void administrationEscapesNamesAndHidesItsNavigationFromUsers() throws Exception {
        mvc.perform(put("/api/admin/usuarios/player2@eventos.test").with(csrf().asHeader())
            .header("Authorization",bearer(1)).contentType("application/json")
            .content("{\"name\":\"<script>alert(1)</script>\",\"displayName\":\"Gamer\",\"role\":\"USER\"}"))
            .andExpect(status().isOk());
        mvc.perform(get("/admin/usuarios").with(user("player1@eventos.test").roles("ADMIN")))
            .andExpect(status().isOk()).andExpect(content().string(not(containsString("<script>alert(1)</script>"))))
            .andExpect(content().string(containsString("&lt;script&gt;alert(1)&lt;/script&gt;")));
        mvc.perform(get("/eventos").with(user("player2@eventos.test")))
            .andExpect(content().string(not(containsString("Administrar usuarios"))));
    }
}
