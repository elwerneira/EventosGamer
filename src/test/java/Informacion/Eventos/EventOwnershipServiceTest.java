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

class EventOwnershipServiceTest {
@Test void missingAndForeignEventsAreRejected() {
 var owners=mock(EventOwnershipRepository.class); var events=mock(EventJpaRepository.class);
 var service=new EventOwnershipService(owners,events);
 when(events.existsById(1L)).thenReturn(true); when(owners.findById(1L)).thenReturn(Optional.of(new EventOwnership(1,"owner@test.cl")));
 assertThat(service.isOwner(1,"owner@test.cl")).isTrue(); service.requireOwner(1,"owner@test.cl");
 assertThatThrownBy(()->service.requireOwner(1,"other@test.cl")).isInstanceOf(ResponseStatusException.class);
 assertThatThrownBy(()->service.requireOwner(999,"owner@test.cl")).isInstanceOf(ResponseStatusException.class);
}
}
