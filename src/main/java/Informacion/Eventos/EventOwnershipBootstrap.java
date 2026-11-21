package Informacion.Eventos;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1)
public class EventOwnershipBootstrap implements ApplicationRunner {
    private final EventJpaRepository events;
    private final EventOwnershipRepository owners;

    public EventOwnershipBootstrap(EventJpaRepository events, EventOwnershipRepository owners) {
        this.events = events; this.owners = owners;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // La migración asigna solo los eventos sin autor; nunca reemplaza una propiedad existente.
        for (var event : events.findAll()) {
            if (!owners.existsById(event.id)) owners.save(new EventOwnership(event.id, "player1@eventos.test"));
        }
    }
}
