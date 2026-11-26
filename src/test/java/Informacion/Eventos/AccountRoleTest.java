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

class AccountRoleTest {
@Test void storesAssignedRoleAndEmail() {
 var role=new AccountRole("admin@example.test",AccountRole.Role.ADMIN);
 assertThat(role.email).isEqualTo("admin@example.test"); assertThat(role.role).isEqualTo(AccountRole.Role.ADMIN);
}
}
