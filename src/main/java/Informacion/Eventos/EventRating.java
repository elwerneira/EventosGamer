package Informacion.Eventos;

import jakarta.persistence.*;

@Entity
@Table(name = "event_ratings", uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "author_email"}))
public class EventRating {
    @Id @Column(length = 36) String id;
    @Column(name = "event_id", nullable = false) Long eventId;
    @Column(name = "author_email", nullable = false, length = 254) String authorEmail;
    @Column(nullable = false) int score;
    protected EventRating() {}
}
