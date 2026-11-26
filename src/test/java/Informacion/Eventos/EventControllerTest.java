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

class EventControllerTest {
@Test void searchPopulatesFiltersAndUsesTheEventsView() {
 var events=mock(EventRepository.class);
 when(events.search(null,null,null,null,null,null,null)).thenReturn(List.of());
 when(events.videoGames()).thenReturn(List.of("Minecraft"));
 var model=new ExtendedModelMap();
 assertThat(new EventController(events).list(" ",null," ",null,null,null,null,model)).isEqualTo("Eventos");
 assertThat(model.get("videoGames")).isEqualTo(List.of("Minecraft")); assertThat(model.get("events")).isEqualTo(List.of());
}
}
