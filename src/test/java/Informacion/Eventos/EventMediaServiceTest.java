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

class EventMediaServiceTest {
@Test void invalidFileCannotBeStored() {
 var db=mock(EventMediaRepository.class); var ownership=mock(EventOwnershipService.class);
 var service=new EventMediaService(db,ownership);
 var file=new MockMultipartFile("file","payload.html","text/html","<script>".getBytes());
 assertThatThrownBy(()->service.upload(1,"owner@test.cl",file,"demo")).isInstanceOf(ResponseStatusException.class);
 verify(ownership).requireOwner(1,"owner@test.cl"); verifyNoInteractions(db);
}
}
