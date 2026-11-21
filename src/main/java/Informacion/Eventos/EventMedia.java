package Informacion.Eventos;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_media")
public class EventMedia {
    @Id @Column(length = 36) String id;
    @Column(name = "event_id", nullable = false) Long eventId;
    @Column(nullable = false, length = 5) String kind;
    @Column(name = "content_type", nullable = false, length = 32) String contentType;
    @Column(nullable = false, length = 240) String caption;
    @Column(name = "created_at", nullable = false) LocalDateTime createdAt;
    @Lob @Column(nullable = false, columnDefinition = "LONGBLOB") byte[] content;
    protected EventMedia() {}
}
