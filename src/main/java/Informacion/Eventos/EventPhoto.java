package Informacion.Eventos;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record EventPhoto(@Column(length = 80) String filename, @Column(length = 200) String alt,
        @Column(length = 200) String credit, @Column(name = "source_url", length = 400) String sourceUrl,
        @Column(name = "license_url", length = 120) String licenseUrl) {}
