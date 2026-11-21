package Informacion.Eventos;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_shares")
public class EventShare {
    @Id @Column(name = "event_id") Long eventId;
    @Column(name = "shared_at", nullable = false) LocalDateTime sharedAt;
    protected EventShare() {}
}
