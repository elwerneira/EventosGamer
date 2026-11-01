package Informacion.Eventos;

import jakarta.persistence.*;

@Entity
@Table(name = "account_roles")
public class AccountRole {
    enum Role { USER, ADMIN }
    @Id @Column(length = 254)
    String email;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10)
    Role role;

    protected AccountRole() {}

    AccountRole(String email, Role role) {
        this.email = email;
        this.role = role;
    }
}
