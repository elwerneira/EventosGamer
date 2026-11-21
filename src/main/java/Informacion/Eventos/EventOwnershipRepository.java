package Informacion.Eventos;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventOwnershipRepository extends JpaRepository<EventOwnership, Long> {
    List<EventOwnership> findByAuthorEmail(String email);
}
