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

class ApiControllerTest {
@Test void privateDetailRejectsUnknownEvent() {
 var events=mock(EventRepository.class); when(events.find(99)).thenReturn(Optional.empty());
 var controller=new ApiController(mock(org.springframework.security.authentication.AuthenticationManager.class),mock(JwtService.class),events,mock(AccountRepository.class),mock(EventJpaRepository.class));
 assertThatThrownBy(()->controller.detail(99)).isInstanceOf(ResponseStatusException.class);
 assertThat(controller.invalidLogin().getBody()).containsEntry("error","invalid_request");
}
}
