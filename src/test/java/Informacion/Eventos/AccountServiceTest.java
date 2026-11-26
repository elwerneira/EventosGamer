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

class AccountServiceTest {
@Test void authenticationLoadsFreshRolesAndRejectsUnknownUsers() {
 var db=mock(AccountRepository.class); var roles=mock(AccountRoleRepository.class);
 var account=new Account(new RegistrationForm("Player","player@test.cl","password123"),"hash");
 when(db.findById("player@test.cl")).thenReturn(Optional.of(account));
 when(roles.findById("player@test.cl")).thenReturn(Optional.of(new AccountRole("player@test.cl",AccountRole.Role.ADMIN)));
 var service=new AccountService(db,new BCryptPasswordEncoder(),roles,"password123");
 assertThat(service.loadUserByUsername(" PLAYER@TEST.CL ").getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
 assertThatThrownBy(()->service.loadUserByUsername("missing@test.cl")).isInstanceOf(org.springframework.security.core.userdetails.UsernameNotFoundException.class);
}
}
