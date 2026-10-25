package Informacion.Eventos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ProfileForm(
        @NotBlank(message = "Ingresa tu nombre de usuario.")
        @Size(max = 40, message = "Usa hasta 40 caracteres.") String displayName,
        @NotBlank(message = "Indica tus juegos de interés.")
        @Size(max = 160, message = "Usa hasta 160 caracteres.") String games,
        @NotBlank(message = "Selecciona un avatar.")
        @Pattern(regexp = "explorador|mago|piloto|guerrero", message = "Selecciona un avatar de la lista.") String avatar,
        Boolean notifications) {
    public ProfileForm {
        displayName = displayName == null ? "" : displayName.trim();
        games = games == null ? "" : games.trim();
        notifications = Boolean.TRUE.equals(notifications);
    }
}
