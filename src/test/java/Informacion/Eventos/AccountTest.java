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

class AccountTest {
@Test void assignedIdAndLifecycleNeverReplaceANewAccount() {
 var a=new Account(new RegistrationForm("Jugador", " USER@EXAMPLE.TEST ", "password123"),"hash");
 assertThat(a.getId()).isEqualTo("user@example.test"); assertThat(a.isNew()).isTrue();
 a.markExisting(); assertThat(a.isNew()).isFalse(); assertThat(a.displayName).isEqualTo("Jugador");
}
}
