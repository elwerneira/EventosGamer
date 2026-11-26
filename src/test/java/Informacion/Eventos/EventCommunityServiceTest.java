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

class EventCommunityServiceTest {
@Test void invalidPublicOriginCannotGenerateShareLinks() {
 assertThatThrownBy(()->new EventCommunityService(mock(EventRepository.class),mock(EventOwnershipRepository.class),
 mock(EventOwnershipService.class),mock(EventCommentRepository.class),mock(EventRatingRepository.class),
 mock(EventMediaRepository.class),mock(EventShareRepository.class),mock(AccountRepository.class),"https://user:secret@example.test"))
 .isInstanceOf(IllegalArgumentException.class);
}
}
