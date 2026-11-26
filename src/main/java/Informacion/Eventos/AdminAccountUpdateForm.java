package Informacion.Eventos;

import jakarta.validation.constraints.*;

public record AdminAccountUpdateForm(
        @NotBlank @Size(max = 80) String name,
        @NotBlank @Size(max = 40) String displayName,
        @NotNull AccountRole.Role role) {
    public AdminAccountUpdateForm {
        name = name == null ? "" : name.trim();
        displayName = displayName == null ? "" : displayName.trim();
    }
}
