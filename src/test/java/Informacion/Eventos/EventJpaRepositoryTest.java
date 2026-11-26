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
class EventJpaRepositoryTest {

 @Autowired EntityManager em;
 @Autowired EventJpaRepository repository;
@Test void searchCombinesGameAndCountryFiltersInTheDatabase() {
  var found=repository.search(null,null,null,null,LocalDateTime.of(2020,1,1,0,0),null,"Minecraft","Argentina","Online");
  assertThat(found).extracting(e->e.id).containsExactly(4L);
  assertThat(repository.countries()).contains("Chile","Argentina");
 }
}
