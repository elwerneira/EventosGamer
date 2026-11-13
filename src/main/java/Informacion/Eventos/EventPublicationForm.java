package Informacion.Eventos;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public record EventPublicationForm(
        @NotBlank @Size(max = 120) String title,
        @NotBlank @Size(max = 60) String category,
        @NotBlank @Size(max = 80) String location,
        @NotNull @Future @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startsAt,
        @NotBlank @Size(max = 1500) String description,
        @NotBlank @Size(max = 100) String videoGame,
        @NotBlank @Size(max = 80) String country,
        @NotBlank @Pattern(regexp = "Presencial|Online|Híbrido") String modality,
        @NotBlank @Size(max = 200) String address,
        @NotBlank @Size(max = 200) String organizers,
        @Size(max = 300) @Pattern(regexp = "(?:https?://[^\\s<>\"']+)?") String accessUrl,
        @NotBlank @Size(max = 1000) String requirements,
        @NotBlank @Size(max = 1000) String registrationInfo) {
    @AssertTrue(message = "La fecha debe estar dentro del año 2037 o anterior.")
    public boolean isSupportedDate() { return startsAt == null || startsAt.getYear() <= 2037; }

    @AssertTrue(message = "Un evento online o híbrido requiere un enlace http o https.")
    public boolean isOnlineAccessValid() {
        return "Presencial".equals(modality) || modality == null || accessUrl != null && !accessUrl.isBlank();
    }
}
