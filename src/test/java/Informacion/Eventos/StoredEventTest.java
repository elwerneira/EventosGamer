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

class StoredEventTest {
@Test void missingDetailsHaveSafeFallbacks() {
 var s=new StoredEvent(); s.id=22L; s.title="Encuentro";
 var e=s.toEvent(); assertThat(e.id()).isEqualTo(22); assertThat(e.videoGame()).isEqualTo("Por confirmar");
 assertThat(e.accessUrl()).isEmpty(); assertThat(e.photos()).isEmpty(); assertThat(e.popular()).isFalse();
}
}
