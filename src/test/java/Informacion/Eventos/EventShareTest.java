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
class EventShareTest {

 @Autowired EntityManager em;
@Test void sharedEventRetainsPublicationTime() {
  var s=new EventShare();s.eventId=1L;s.sharedAt=LocalDateTime.of(2030,1,1,0,0);
  em.persist(s);em.flush();em.clear();assertThat(em.find(EventShare.class,1L).sharedAt).isEqualTo(s.sharedAt);
 }
}
