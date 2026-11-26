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
class EventShareRepositoryTest {

 @Autowired EntityManager em;
 @Autowired EventShareRepository repository;
@Test void communityFeedUsesMostRecentlySharedFirst() {
  var a=new EventShare();a.eventId=1L;a.sharedAt=LocalDateTime.of(2026,1,1,0,0);
  var b=new EventShare();b.eventId=2L;b.sharedAt=a.sharedAt.plusDays(1);
  repository.saveAllAndFlush(List.of(a,b));
  assertThat(repository.findAllByOrderBySharedAtDesc()).extracting(s->s.eventId).containsExactly(2L,1L);
 }
}
