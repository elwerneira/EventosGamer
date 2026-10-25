package Informacion.Eventos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Locale;

public record RegistrationForm(
        @NotBlank(message = "Ingresa tu nombre.")
        @Size(max = 80, message = "Usa hasta 80 caracteres.") String name,
        @NotBlank(message = "Ingresa tu correo.")
        @Email(message = "Ingresa un correo válido.")
        @Size(max = 254, message = "Usa hasta 254 caracteres.") String email,
        @NotBlank(message = "Ingresa tu contraseña.")
        @Size(min = 10, max = 64, message = "Usa entre 10 y 64 caracteres.") String password) {
    public RegistrationForm {
        name = name == null ? "" : name.trim();
        email = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
