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
class EventCommentTest {

 @Autowired EntityManager em;
@Test void commentPersistsItsAuthorTextAndTimestamp() {
  var c=comment("s6-entity-comment",1,"Buen evento",LocalDateTime.of(2030,1,1,0,0));em.persist(c);em.flush();em.clear();
  var loaded=em.find(EventComment.class,c.id);assertThat(loaded.body).isEqualTo("Buen evento");
  assertThat(loaded.authorEmail).isEqualTo("player2@eventos.test");assertThat(loaded.createdAt).isEqualTo(c.createdAt);
 }
}
