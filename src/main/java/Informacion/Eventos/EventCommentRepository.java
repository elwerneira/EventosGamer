package Informacion.Eventos;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventCommentRepository extends JpaRepository<EventComment, String> {
    List<EventComment> findTop50ByEventIdOrderByCreatedAtDesc(long eventId);
    long countByEventId(long eventId);
}
