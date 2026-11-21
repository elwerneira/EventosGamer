package Informacion.Eventos;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EventOwnershipService {
    private final EventOwnershipRepository owners;
    private final EventJpaRepository events;

    public EventOwnershipService(EventOwnershipRepository owners, EventJpaRepository events) {
        this.owners = owners; this.events = events;
    }

    public boolean isOwner(long eventId, String email) {
        return owners.findById(eventId).map(owner -> owner.authorEmail.equals(email)).orElse(false);
    }

    public void requireOwner(long eventId, String email) {
        if (!events.existsById(eventId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (!isOwner(eventId, email)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
}
