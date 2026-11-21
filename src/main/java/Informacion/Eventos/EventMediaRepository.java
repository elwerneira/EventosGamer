package Informacion.Eventos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventMediaRepository extends JpaRepository<EventMedia, String> {
    interface Info {
        String getId();
        String getKind();
        String getContentType();
        String getCaption();
        LocalDateTime getCreatedAt();
    }
    List<Info> findByEventIdOrderByCreatedAtDesc(long eventId);
    Optional<EventMedia> findByIdAndEventId(String id, long eventId);
}
