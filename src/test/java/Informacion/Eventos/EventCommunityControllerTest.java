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

class EventCommunityControllerTest {
@Test void mineUsesTheAuthenticatedAuthorAndCorrectView() {
 var community=mock(EventCommunityService.class); when(community.mine("player@test.cl")).thenReturn(List.of());
 var c=new EventCommunityController(community,mock(EventMediaService.class)); var model=new ExtendedModelMap();
 assertThat(c.mine(new UsernamePasswordAuthenticationToken("player@test.cl",null),model)).isEqualTo("Comunidad");
 verify(community).mine("player@test.cl"); assertThat(model.get("heading")).isEqualTo("Mis eventos");
}
}
