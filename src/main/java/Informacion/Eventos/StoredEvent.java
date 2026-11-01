package Informacion.Eventos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;

@Entity
@Table(name = "events")
public class StoredEvent {
    @Id
    Long id;
    @Column(nullable = false, length = 120)
    String title;
    @Column(nullable = false, length = 60)
    String category;
    @Column(nullable = false, length = 80)
    String location;
    @Column(name = "starts_at", nullable = false)
    LocalDateTime startsAt;
    @Column(nullable = false, length = 1500)
    String description;
    @Column(nullable = false, length = 200)
    String address;
    @Column(nullable = false, length = 200)
    String organizers;
    @Column(nullable = false, length = 200)
    String parking;
    @Column(nullable = false, length = 200)
    String transport;
    @Column(nullable = false, length = 200)
    String food;
    @Column(nullable = false, length = 200)
    String accommodation;
    @Column(nullable = false, length = 300)
    String exhibitors;
    @Column(nullable = false)
    boolean featured;
    @OneToOne(mappedBy = "event")
    EventDetails details;

    protected StoredEvent() {
    }

    Event toEvent() {
        return new Event(id, title, category, location, startsAt, description, address, organizers,
                parking, transport, food, accommodation, exhibitors, featured,
                details == null ? "Por confirmar" : details.videoGame,
                details == null ? "Por confirmar" : details.country,
                details == null ? "Por confirmar" : details.modality,
                details == null ? "" : details.accessUrl,
                details == null ? "Por confirmar con la organización." : details.requirements,
                details == null ? "Por confirmar con la organización." : details.registrationInfo,
                details == null ? null : details.publishedAt,
                details != null && details.popular,
                details == null ? java.util.List.of() : java.util.List.copyOf(details.photos));
    }
}
