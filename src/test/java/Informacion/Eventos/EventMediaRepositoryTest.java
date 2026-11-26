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
class EventMediaRepositoryTest {

 @Autowired EntityManager em;
 @Autowired EventMediaRepository repository;
@Test void mediaLookupCannotCrossEventBoundaries() {
  repository.saveAndFlush(media("s6-media",1));em.clear();
  assertThat(repository.findByIdAndEventId("s6-media",2)).isEmpty();
  assertThat(repository.findByIdAndEventId("s6-media",1).orElseThrow().content).containsExactly(1,2,3);
  assertThat(repository.findByEventIdOrderByCreatedAtDesc(1)).singleElement().satisfies(info->{
   assertThat(info.getId()).isEqualTo("s6-media");assertThat(info.getKind()).isEqualTo("VIDEO");
   assertThat(info.getContentType()).isEqualTo("video/mp4");assertThat(info.getCaption()).isEqualTo("Clip técnico");
   assertThat(info.getCreatedAt()).isNotNull();
  });
 }
}
