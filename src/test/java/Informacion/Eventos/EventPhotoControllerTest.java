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

class EventPhotoControllerTest {
@Test void rejectsTraversalAndMissingFiles() {
 var c=new EventPhotoController();
 assertThatThrownBy(()->c.photo("../secret.jpg")).isInstanceOf(ResponseStatusException.class);
 assertThatThrownBy(()->c.photo("missing.jpg")).isInstanceOf(ResponseStatusException.class);
}
}
