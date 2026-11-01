package Informacion.Eventos;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "accounts")
public class Account implements Persistable<String> {
    @Id
    @Column(length = 254)
    String email;
    @Column(nullable = false, length = 80)
    String name;
    @Column(name = "password_hash", nullable = false, length = 100)
    String passwordHash;
    @Column(name = "display_name", nullable = false, length = 40)
    String displayName;
    @Column(nullable = false, length = 160)
    String games = "";
    @Column(nullable = false, length = 20)
    String avatar = "explorador";
    @Column(nullable = false)
    boolean notifications;
    @Transient
    private boolean newAccount = true;

    protected Account() {
    }

    public Account(RegistrationForm form, String passwordHash) {
        this.email = form.email();
        this.name = form.name();
        this.passwordHash = passwordHash;
        this.displayName = form.name().substring(0, Math.min(40, form.name().length()));
    }

    @Override
    public String getId() {
        return email;
    }

    // El correo es un ID asignado: un registro nuevo debe insertar, nunca reemplazar otra cuenta.
    @Override
    public boolean isNew() {
        return newAccount;
    }

    @PostLoad
    @PostPersist
    void markExisting() {
        newAccount = false;
    }
}
