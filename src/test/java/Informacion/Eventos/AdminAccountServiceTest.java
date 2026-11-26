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

class AdminAccountServiceTest {
@Test void directCallsAlsoRequireCurrentAdministratorRole() {
 var roles=mock(AccountRoleRepository.class);
 var service=new AdminAccountService(mock(AccountRepository.class),roles,new BCryptPasswordEncoder(),mock(EntityManager.class));
 try {
 SecurityContextHolder.clearContext(); assertThatThrownBy(service::list).isInstanceOf(ResponseStatusException.class);
 SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user@test.cl",null,List.of()));
 when(roles.findById("user@test.cl")).thenReturn(Optional.of(new AccountRole("user@test.cl",AccountRole.Role.USER)));
 assertThatThrownBy(service::list).isInstanceOf(ResponseStatusException.class);
 } finally {SecurityContextHolder.clearContext();}
}
}
