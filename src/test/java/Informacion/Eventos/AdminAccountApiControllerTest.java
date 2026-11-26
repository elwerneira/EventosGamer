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

class AdminAccountApiControllerTest {
@Test void apiReturnsConflictWithoutExposingDatabaseDetails() {
 var c=new AdminAccountApiController(mock(AdminAccountService.class));
 assertThat(c.conflict().getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
 assertThat(c.conflict().getBody()).containsExactly(entry("error","account_conflict"));
 assertThat(c.invalid().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
}
}
