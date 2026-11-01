package Informacion.Eventos;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "event_details")
public class EventDetails {
    @Id @Column(name = "event_id")
    Long eventId;
    @OneToOne @MapsId @JoinColumn(name = "event_id")
    StoredEvent event;
    @Column(name = "video_game", nullable = false, length = 100)
    String videoGame;
    @Column(nullable = false, length = 80)
    String country;
    @Column(nullable = false, length = 15)
    String modality;
    @Column(name = "access_url", nullable = false, length = 300)
    String accessUrl;
    @Column(nullable = false, length = 1000)
    String requirements;
    @Column(name = "registration_info", nullable = false, length = 1000)
    String registrationInfo;
    @Column(name = "published_at", nullable = false)
    LocalDateTime publishedAt;
    @Column(nullable = false)
    boolean popular;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "event_photos", joinColumns = @JoinColumn(name = "event_id"))
    @OrderColumn(name = "photo_order")
    List<EventPhoto> photos;

    protected EventDetails() {}
}
