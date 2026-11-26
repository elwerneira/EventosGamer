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

class LoginRateLimitFilterTest {
@Test void limitAndNonLoginRoutesAreHandledIndependently() {
 var filter=new LoginRateLimitFilter(1,Clock.fixed(Instant.EPOCH,ZoneOffset.UTC));
 assertThat(filter.allow("one")).isTrue(); assertThat(filter.allow("one")).isFalse(); assertThat(filter.allow("two")).isTrue();
 assertThat(filter.shouldNotFilter(new MockHttpServletRequest("GET","/login"))).isTrue();
 assertThat(filter.shouldNotFilter(new MockHttpServletRequest("POST","/login"))).isFalse();
 assertThatThrownBy(()->new LoginRateLimitFilter(0)).isInstanceOf(IllegalArgumentException.class);
}
}
