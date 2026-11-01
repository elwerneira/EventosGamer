package Informacion.Eventos;

import java.time.LocalDateTime;
import java.util.List;

public record Event(long id, String title, String category, String location, LocalDateTime startsAt,
        String description, String address, String organizers, String parking, String transport,
        String food, String accommodation, String exhibitors, boolean featured,
        String videoGame, String country, String modality, String accessUrl, String requirements,
        String registrationInfo, LocalDateTime publishedAt, boolean popular, List<EventPhoto> photos) {
    public EventSummary summary() {
        return new EventSummary(id, title, category, location, startsAt, videoGame, country, modality, publishedAt);
    }
}
