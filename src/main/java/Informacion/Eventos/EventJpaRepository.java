package Informacion.Eventos;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EventJpaRepository extends JpaRepository<StoredEvent, Long> {
    @Query("""
        SELECT e FROM StoredEvent e LEFT JOIN e.details d WHERE e.startsAt >= :now
        AND (:category IS NULL OR e.category = :category)
        AND (:start IS NULL OR (e.startsAt >= :start AND e.startsAt < :end))
        AND (:location IS NULL OR e.location = :location)
        AND (:name IS NULL OR LOCATE(LOWER(:name), LOWER(e.title)) > 0)
        AND (:videoGame IS NULL OR LOCATE(LOWER(:videoGame), LOWER(d.videoGame)) > 0)
        AND (:country IS NULL OR LOWER(d.country) = LOWER(:country))
        AND (:modality IS NULL OR LOWER(d.modality) = LOWER(:modality))
        ORDER BY e.startsAt
        """)
    List<StoredEvent> search(String category, LocalDateTime start, LocalDateTime end,
            String location, LocalDateTime now, String name, String videoGame, String country, String modality);

    @Query("SELECT DISTINCT d.videoGame FROM EventDetails d ORDER BY d.videoGame")
    List<String> videoGames();

    @Query("SELECT DISTINCT d.country FROM EventDetails d ORDER BY d.country")
    List<String> countries();

    @Query("SELECT DISTINCT e.category FROM StoredEvent e ORDER BY e.category")
    List<String> categories();

    @Query("SELECT DISTINCT e.location FROM StoredEvent e ORDER BY e.location")
    List<String> locations();
}
