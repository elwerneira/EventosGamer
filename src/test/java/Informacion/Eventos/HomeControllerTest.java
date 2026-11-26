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

class HomeControllerTest {
@Test void homePopulatesEmptyListsAndUsesTheHomeView() {
 var events=mock(EventRepository.class); when(events.search(null,null,null)).thenReturn(List.of());
 var model=new ExtendedModelMap(); assertThat(new HomeController(events).home(model)).isEqualTo("Home");
 assertThat(model.get("events")).isEqualTo(List.of()); assertThat(model.get("featured")).isEqualTo(List.of());
}
}
