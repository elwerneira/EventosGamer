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

class EventPublicationFormTest {
@Test void onlineEventsRequireALinkAndSupportedDate() {
 var online=new EventPublicationForm("A","B","C",LocalDateTime.of(2038,1,1,0,0),"D","E","F","Online","G","H","","I","J");
 assertThat(online.isSupportedDate()).isFalse(); assertThat(online.isOnlineAccessValid()).isFalse();
 var presencial=new EventPublicationForm("A","B","C",null,"D","E","F","Presencial","G","H",null,"I","J");
 assertThat(presencial.isSupportedDate()).isTrue(); assertThat(presencial.isOnlineAccessValid()).isTrue();
}
}
