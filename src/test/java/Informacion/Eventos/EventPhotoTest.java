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

class EventPhotoTest {
@Test void attributionSurvivesAsAValueObject() {
 var photo=new EventPhoto("demo.jpg","Evento gamer","Autor","https://example.test/photo","https://example.test/license");
 assertThat(photo.filename()).isEqualTo("demo.jpg"); assertThat(photo.alt()).isEqualTo("Evento gamer");
 assertThat(photo.credit()).isEqualTo("Autor"); assertThat(photo.sourceUrl()).endsWith("/photo");
 assertThat(photo.licenseUrl()).endsWith("/license");
}
}
