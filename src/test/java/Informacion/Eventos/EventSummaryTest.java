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

class EventSummaryTest {
@Test void summaryRetainsSearchFields() {
 var time=LocalDateTime.of(2030,1,1,18,0);
 var s=new EventSummary(9,"Torneo","Esports","Santiago",time,"Valorant","Chile","Online",time);
 assertThat(s.title()).isEqualTo("Torneo"); assertThat(s.videoGame()).isEqualTo("Valorant");
 assertThat(s.country()).isEqualTo("Chile"); assertThat(s.startsAt()).isEqualTo(time); assertThat(s.id()).isEqualTo(9);
}
}
