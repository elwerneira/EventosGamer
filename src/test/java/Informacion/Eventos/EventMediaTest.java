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
class EventMediaTest {

 @Autowired EntityManager em;
@Test void mediaPersistsExactBinaryContent() {
  var m=media("s6-entity-media",1);em.persist(m);em.flush();em.clear();
  var loaded=em.find(EventMedia.class,m.id);assertThat(loaded.content).containsExactly(1,2,3);
  assertThat(loaded.contentType).isEqualTo("video/mp4");assertThat(loaded.caption).isEqualTo("Clip técnico");
 }
}
