package Informacion.Eventos;

import jakarta.persistence.*;

@Entity
@Table(name = "event_owners")
public class EventOwnership {
    @Id @Column(name = "event_id") Long eventId;
    @Column(name = "author_email", nullable = false, length = 254) String authorEmail;
    protected EventOwnership() {}
    EventOwnership(long eventId, String email) { this.eventId = eventId; this.authorEmail = email; }
}
