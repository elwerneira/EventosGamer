package Informacion.Eventos;
import static Informacion.Eventos.S6JpaFixtures.*;
import static org.assertj.core.api.Assertions.*;
import java.time.*;
import java.util.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class EventDetailsTest {

 @Autowired EntityManager em;
@Test void eventDetailsPreserveGameAndPhotoAttribution() {
  var details=em.find(EventDetails.class,1L);
  assertThat(details.videoGame).isNotBlank();assertThat(details.event.toEvent().videoGame()).isEqualTo(details.videoGame);
  assertThat(details.event.toEvent().photos()).containsExactlyElementsOf(details.photos);
 }
}
