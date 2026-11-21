package Informacion.Eventos;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_comments")
public class EventComment {
    @Id @Column(length = 36) String id;
    @Column(name = "event_id", nullable = false) Long eventId;
    @Column(name = "author_email", nullable = false, length = 254) String authorEmail;
    @Column(nullable = false, length = 500) String body;
    @Column(name = "created_at", nullable = false) LocalDateTime createdAt;
    protected EventComment() {}
}
