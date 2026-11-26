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
class EventRatingTest {

 @Autowired EntityManager em;
@Test void ratingPersistsScoreAndAccount() {
  var r=rating("s6-entity-rating",1,5);em.persist(r);em.flush();em.clear();
  var loaded=em.find(EventRating.class,r.id);assertThat(loaded.score).isEqualTo(5);
  assertThat(loaded.authorEmail).isEqualTo("player2@eventos.test");
 }
}
