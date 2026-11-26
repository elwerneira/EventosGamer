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
class AccountRepositoryTest {

 @Autowired EntityManager em;
 @Autowired AccountRepository repository;
@Test void findByIdRestoresExistingStateAndPreservesPasswordHash() {
  var existing=repository.findById("player1@eventos.test").orElseThrow();var hash=existing.passwordHash;
  assertThat(existing.isNew()).isFalse(); assertThat(repository.findById("unknown@test.cl")).isEmpty();
  assertThat(repository.findById(existing.email).orElseThrow().passwordHash).isEqualTo(hash);
 }
}
