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

class ProfileFormTest {
@Test void trimsFieldsAndDefaultsNotificationsToFalse() {
 var f=new ProfileForm(" Player "," Minecraft ","mago",null);
 assertThat(f.displayName()).isEqualTo("Player"); assertThat(f.games()).isEqualTo("Minecraft");
 assertThat(f.notifications()).isFalse();
 var empty=new ProfileForm(null,null,"mago",true);
 assertThat(empty.displayName()).isEmpty(); assertThat(empty.games()).isEmpty(); assertThat(empty.notifications()).isTrue();
}
}
