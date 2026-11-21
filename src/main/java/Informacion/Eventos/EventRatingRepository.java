package Informacion.Eventos;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EventRatingRepository extends JpaRepository<EventRating, String> {
    Optional<EventRating> findByEventIdAndAuthorEmail(long eventId, String email);
    long countByEventId(long eventId);
    @Query("SELECT AVG(r.score) FROM EventRating r WHERE r.eventId = :eventId")
    Double average(long eventId);
}
