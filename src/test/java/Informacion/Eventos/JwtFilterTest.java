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

class JwtFilterTest {
@Test void invalidAuthorizationStopsTheChain() throws Exception {
 var filter=new JwtFilter(mock(JwtService.class),mock(AccountService.class));
 var request=new MockHttpServletRequest("GET","/api/eventos/1"); request.addHeader("Authorization","Basic x");
 var response=new MockHttpServletResponse(); var chain=mock(jakarta.servlet.FilterChain.class);
 try {filter.doFilterInternal(request,response,chain);
 assertThat(response.getStatus()).isEqualTo(401); assertThat(response.getHeader("WWW-Authenticate")).isEqualTo("Bearer");
 verifyNoInteractions(chain);} finally {SecurityContextHolder.clearContext();}
}
}
