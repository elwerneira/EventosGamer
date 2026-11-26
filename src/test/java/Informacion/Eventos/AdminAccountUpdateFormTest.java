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

class AdminAccountUpdateFormTest {
@Test void trimsNamesAndPreservesRequestedRole() {
 var f=new AdminAccountUpdateForm(" Player "," Gamer ",AccountRole.Role.USER);
 assertThat(f.name()).isEqualTo("Player"); assertThat(f.displayName()).isEqualTo("Gamer");
 assertThat(new AdminAccountUpdateForm(null,null,null).displayName()).isEmpty();
}
}
