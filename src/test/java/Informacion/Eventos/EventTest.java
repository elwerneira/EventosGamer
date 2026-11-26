package Informacion.Eventos;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import java.time.*;
import jakarta.persistence.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.mock.web.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BeanPropertyBindingResult;

class EventTest {
@Test void publicSummaryDoesNotIncludePrivateDetails() {
 var s=new StoredEvent(); s.id=9L; s.title="Torneo"; s.address="Dirección privada";
 var summary=s.toEvent().summary(); assertThat(summary.id()).isEqualTo(9); assertThat(summary.title()).isEqualTo("Torneo");
 assertThat(Arrays.stream(EventSummary.class.getRecordComponents()).map(c->c.getName()))
  .doesNotContain("address","photos","description");
}
}
