package Informacion.Eventos;

import java.time.LocalDateTime;

public record EventSummary(long id, String title, String category, String location, LocalDateTime startsAt,
        String videoGame, String country, String modality, LocalDateTime publishedAt) {}
