package Informacion.Eventos;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventPublicationService {
    private final EntityManager persistence;

    public EventPublicationService(EntityManager persistence) { this.persistence = persistence; }

    @Transactional
    public Event publish(EventPublicationForm form) {
        var event = new StoredEvent();
        event.title = form.title().trim();
        event.category = form.category().trim();
        event.location = form.location().trim();
        event.startsAt = form.startsAt();
        event.description = form.description().trim();
        event.address = form.address().trim();
        event.organizers = form.organizers().trim();
        event.parking = "Consultar con la organización.";
        event.transport = "Consultar con la organización.";
        event.food = "Consultar con la organización.";
        event.accommodation = "Consultar con la organización.";
        event.exhibitors = "Por confirmar.";
        persistence.persist(event);
        var details = new EventDetails();
        details.event = event;
        details.videoGame = form.videoGame().trim();
        details.country = form.country().trim();
        details.modality = form.modality();
        details.accessUrl = form.accessUrl() == null ? "" : form.accessUrl().trim();
        details.requirements = form.requirements().trim();
        details.registrationInfo = form.registrationInfo().trim();
        details.publishedAt = LocalDateTime.now();
        details.photos = new ArrayList<>();
        persistence.persist(details);
        event.details = details;
        persistence.flush();
        return event.toEvent();
    }
}
