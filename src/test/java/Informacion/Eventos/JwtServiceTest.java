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

class JwtServiceTest {
@Test void issuedTokenRoundTripsAndRejectsTampering() {
 var service=new JwtService("dGVzdC1vbmx5LWdyZWVnYW1lLWtleS0zMi1ieXRlcy1taW5pbXVt");
 var token=service.issue(new UsernamePasswordAuthenticationToken("player@test.cl",null,List.of()));
 assertThat(service.subject(token)).isEqualTo("player@test.cl");
 assertThatThrownBy(()->service.subject("invalid")).isInstanceOf(RuntimeException.class);
}
}
