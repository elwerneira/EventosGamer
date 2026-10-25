package Informacion.Eventos;

import java.time.LocalDateTime;

public record Event(long id, String title, String category, String location, LocalDateTime startsAt,
        String description, String address, String organizers, String parking, String transport,
        String food, String accommodation, String exhibitors, boolean featured) {
}
