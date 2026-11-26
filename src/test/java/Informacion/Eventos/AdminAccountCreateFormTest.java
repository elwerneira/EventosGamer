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

class AdminAccountCreateFormTest {
@Test void normalizesNewAccountIdentity() {
 var f=new AdminAccountCreateForm(" Admin "," NEW@EXAMPLE.TEST ","password123",AccountRole.Role.ADMIN);
 assertThat(f.name()).isEqualTo("Admin"); assertThat(f.email()).isEqualTo("new@example.test");
 assertThat(f.role()).isEqualTo(AccountRole.Role.ADMIN);
 assertThat(new AdminAccountCreateForm(null,null,"password123",null).name()).isEmpty();
}
}
