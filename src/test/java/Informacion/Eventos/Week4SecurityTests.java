package Informacion.Eventos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest
@AutoConfigureMockMvc
class Week4SecurityTests {
    @Autowired MockMvc mvc;
    @Autowired AccountService accounts;
    @MockitoSpyBean AccountRepository repository;

    @Test void demoInitializationRunsInsideATransactionAndPreservesExistingAccounts() {
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
            return repository.findById(invocation.getArgument(0, String.class)).isPresent();
        }).when(repository).existsById("player1@eventos.test");
        var before = accounts.profile("player1@eventos.test");
        accounts.run(null);
        assertThat(accounts.profile("player1@eventos.test")).isEqualTo(before);
    }

    @Test void renderedPagesKeepExactlyOneTitleFromTheSharedFragment() throws Exception {
        var title = Pattern.compile("<title[^>]*>([^<]+)</title>");
        for (String path : new String[]{"/", "/eventos", "/login", "/registro", "/perfil", "/eventos/1"}) {
            String html = mvc.perform(get(path).with(user("player1@eventos.test")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            var matcher = title.matcher(html);
            assertThat(matcher.find()).as(path).isTrue();
            assertThat(matcher.group(1)).contains("GREEGAME");
            assertThat(matcher.find()).as("Sin títulos duplicados en " + path).isFalse();
        }
    }
}
