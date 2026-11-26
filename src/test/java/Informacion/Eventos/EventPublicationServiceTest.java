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

class EventPublicationServiceTest {
@Test void publicationPersistsEventDetailsAndAuthenticatedOwner() {
 var em=mock(EntityManager.class);
 doAnswer(call->{Object entity=call.getArgument(0); if(entity instanceof StoredEvent e)e.id=123L; return null;}).when(em).persist(any());
 var form=new EventPublicationForm(" Evento ","Comunidad","Santiago",LocalDateTime.of(2030,1,1,0,0),"Detalle","Minecraft","Chile","Online","No aplica","Organizador",null,"Consultar","Libre");
 var result=new EventPublicationService(em).publish(form,"author@test.cl");
 assertThat(result.title()).isEqualTo("Evento"); assertThat(result.accessUrl()).isEmpty();
 var saved=org.mockito.ArgumentCaptor.forClass(Object.class); verify(em,times(3)).persist(saved.capture());
 assertThat(saved.getAllValues().get(2)).isInstanceOfSatisfying(EventOwnership.class,o->assertThat(o.authorEmail).isEqualTo("author@test.cl"));
 verify(em).flush();
}
}
