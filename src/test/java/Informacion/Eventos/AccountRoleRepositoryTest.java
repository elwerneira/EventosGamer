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
class AccountRoleRepositoryTest {

 @Autowired EntityManager em;
 @Autowired AccountRoleRepository repository;
@Test void findsThePersistedAdministratorRole() {
  assertThat(repository.findById("player1@eventos.test").orElseThrow().role).isEqualTo(AccountRole.Role.ADMIN);
  assertThat(repository.findById("player2@eventos.test").orElseThrow().role).isEqualTo(AccountRole.Role.USER);
 }
}
