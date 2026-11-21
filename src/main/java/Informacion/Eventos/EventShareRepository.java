package Informacion.Eventos;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventShareRepository extends JpaRepository<EventShare, Long> {
    List<EventShare> findAllByOrderBySharedAtDesc();
}
