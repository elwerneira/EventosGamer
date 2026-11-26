package Informacion.Eventos;

import jakarta.validation.constraints.*;
import java.util.Locale;

public record AdminAccountCreateForm(
        @NotBlank @Size(max = 80) String name,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 10, max = 64) String password,
        @NotNull AccountRole.Role role) {
    public AdminAccountCreateForm {
        name = name == null ? "" : name.trim();
        email = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
