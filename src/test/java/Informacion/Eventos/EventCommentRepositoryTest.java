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
class EventCommentRepositoryTest {

 @Autowired EntityManager em;
 @Autowired EventCommentRepository repository;
@Test void recentCommentsAreOrderedAndRestrictedToTheEvent() {
  var now=LocalDateTime.now();repository.saveAndFlush(comment("s6-c1",1,"Primero",now));
  repository.saveAndFlush(comment("s6-c2",1,"Segundo",now.plusSeconds(1)));
  repository.saveAndFlush(comment("s6-c3",2,"Otro evento",now));
  assertThat(repository.findTop50ByEventIdOrderByCreatedAtDesc(1)).extracting(c->c.body).containsExactly("Segundo","Primero");
  assertThat(repository.countByEventId(1)).isEqualTo(2);
 }
}
