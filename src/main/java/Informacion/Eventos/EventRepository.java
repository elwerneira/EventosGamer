package Informacion.Eventos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Repository
public class EventRepository {
    private final EventJpaRepository events;

    public EventRepository(EventJpaRepository events) {
        this.events = events;
    }

    public List<Event> search(String category, LocalDate date, String location) {
        return search(category, date, location, null, null, null, null);
    }

    public List<Event> search(String category, LocalDate date, String location,
            String name, String videoGame, String country, String modality) {
        if (date != null && (date.getYear() < 1900 || date.getYear() > 2100)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return events.search(clean(category), date == null ? null : date.atStartOfDay(),
                date == null ? null : date.plusDays(1).atStartOfDay(), clean(location), LocalDateTime.now(),
                clean(name), clean(videoGame), clean(country), clean(modality))
            .stream().map(StoredEvent::toEvent).toList();
    }

    private static String clean(String value) {
        if (value != null && value.length() > 254) throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        return value == null || value.isBlank() ? null : value.trim();
    }

    public List<String> videoGames() { return events.videoGames(); }

    public List<String> countries() { return events.countries(); }

    public Optional<Event> find(long id) {
        return events.findById(id).map(StoredEvent::toEvent);
    }

    public List<String> categories() {
        return events.categories();
    }

    public List<String> locations() {
        return events.locations();
    }
}
