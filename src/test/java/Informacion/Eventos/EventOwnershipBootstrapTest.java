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

class EventOwnershipBootstrapTest {
@Test void initializationOnlyAssignsUnownedEvents() {
 var events=mock(EventJpaRepository.class); var owners=mock(EventOwnershipRepository.class);
 var first=new StoredEvent(); first.id=1L; var second=new StoredEvent(); second.id=2L;
 when(events.findAll()).thenReturn(List.of(first,second)); when(owners.existsById(1L)).thenReturn(true);
 new EventOwnershipBootstrap(events,owners).run(null);
 var saved=org.mockito.ArgumentCaptor.forClass(EventOwnership.class); verify(owners).save(saved.capture());
 assertThat(saved.getValue().eventId).isEqualTo(2); assertThat(saved.getValue().authorEmail).isEqualTo("player1@eventos.test");
}
}
