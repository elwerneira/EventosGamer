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
class EventRatingRepositoryTest {

 @Autowired EntityManager em;
 @Autowired EventRatingRepository repository;
@Test void averageAndVotesAreCalculatedForTheEvent() {
  repository.saveAndFlush(rating("s6-r1",1,4));
  var second=rating("s6-r2",1,2);second.authorEmail="player3@eventos.test";repository.saveAndFlush(second);
  assertThat(repository.average(1)).isEqualTo(3.0);assertThat(repository.countByEventId(1)).isEqualTo(2);
  assertThat(repository.findByEventIdAndAuthorEmail(1,"player2@eventos.test").orElseThrow().score).isEqualTo(4);
 }
}
