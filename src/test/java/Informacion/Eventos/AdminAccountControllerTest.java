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

class AdminAccountControllerTest {
@Test void duplicateCreationShowsEmailErrorAndPreservesTheForm() {
 var service=mock(AdminAccountService.class); var c=new AdminAccountController(service);
 var form=new AdminAccountCreateForm("Jugador","player@test.cl","password123",AccountRole.Role.USER);
 var errors=new BeanPropertyBindingResult(form,"form");
 when(service.create(form)).thenThrow(new org.springframework.dao.DataIntegrityViolationException("duplicate"));
 assertThat(c.create(form,errors)).isEqualTo("AdminUsuarioNuevo");
 assertThat(errors.hasFieldErrors("email")).isTrue();
}
@Test void missingEditAccountIsNotConvertedToAValidationSuccess() {
 var service=mock(AdminAccountService.class); var c=new AdminAccountController(service);
 var form=new AdminAccountUpdateForm("Jugador","Jugador",AccountRole.Role.USER);
 when(service.update("missing@test.cl",form)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
 assertThatThrownBy(()->c.edit("missing@test.cl",form,new BeanPropertyBindingResult(form,"form"),new ExtendedModelMap()))
  .isInstanceOf(ResponseStatusException.class);
}
@Test void invalidCreateFormNeverReachesPersistence() {
 var service=mock(AdminAccountService.class); var c=new AdminAccountController(service);
 var form=new AdminAccountCreateForm("","bad","short",AccountRole.Role.USER);
 var errors=new BeanPropertyBindingResult(form,"form"); errors.rejectValue("email","invalid");
 assertThat(c.create(form,errors)).isEqualTo("AdminUsuarioNuevo"); verifyNoInteractions(service);
}
}
