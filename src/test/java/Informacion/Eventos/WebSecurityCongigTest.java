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

class WebSecurityCongigTest {
@Test void configuredPasswordEncoderStoresOnlyHashes() {
 var config=new WebSecurityCongig(30); var encoder=config.passwordEncoder();
 var hash=encoder.encode("GamerPassword123");
 assertThat(hash).isNotEqualTo("GamerPassword123"); assertThat(encoder.matches("GamerPassword123",hash)).isTrue();
 assertThat(encoder.matches("wrong",hash)).isFalse();
}
}
