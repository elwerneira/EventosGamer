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
class EventOwnershipRepositoryTest {

 @Autowired EntityManager em;
 @Autowired EventOwnershipRepository repository;
@Test void findsOnlyEventsOwnedByTheRequestedAccount() {
  repository.saveAndFlush(new EventOwnership(1,"player2@eventos.test"));em.clear();
  assertThat(repository.findByAuthorEmail("player2@eventos.test")).extracting(o->o.eventId).containsExactly(1L);
  assertThat(repository.findByAuthorEmail("player1@eventos.test")).extracting(o->o.eventId).doesNotContain(1L);
 }
}
