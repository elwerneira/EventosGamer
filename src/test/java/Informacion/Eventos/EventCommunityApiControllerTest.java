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

class EventCommunityApiControllerTest {
@Test void delegatesPrivateMediaDownloadWithExactEventId() {
 var media=mock(EventMediaService.class); var expected=ResponseEntity.ok(new byte[]{1,2});
 when(media.download(4,"photo")).thenReturn(expected);
 var c=new EventCommunityApiController(mock(EventCommunityService.class),media);
 assertThat(c.download(4,"photo")).isSameAs(expected); verify(media).download(4,"photo");
 assertThat(c.tooLarge().getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
}
}
