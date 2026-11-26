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

class EventRepositoryTest {
@Test void rejectsInvalidSearchAndNormalizesFilters() {
 var db=mock(EventJpaRepository.class); var repo=new EventRepository(db);
 when(db.search(any(),any(),any(),any(),any(),any(),any(),any(),any())).thenReturn(List.of());
 assertThat(repo.search("  ",null," Santiago ")).isEmpty();
 verify(db).search(isNull(),isNull(),isNull(),eq("Santiago"),any(),isNull(),isNull(),isNull(),isNull());
 assertThatThrownBy(()->repo.search(null,LocalDate.of(2101,1,1),null)).isInstanceOf(ResponseStatusException.class);
 assertThatThrownBy(()->repo.search("x".repeat(255),null,null)).isInstanceOf(ResponseStatusException.class);
}
}
