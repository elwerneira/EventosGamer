package Informacion.Eventos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.stereotype.Component;

@Component
public class SecurityAudit {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityAudit.class);

    private String actor(String name) {
        if (name == null) return "unknown";
        String safe = name.replaceAll("[\\p{Cntrl}]", "_");
        return safe.substring(0, Math.min(safe.length(), 80));
    }

    @EventListener
    public void success(AuthenticationSuccessEvent event) {
        LOG.info("security_event=authentication_success actor={}", actor(event.getAuthentication().getName()));
    }

    @EventListener
    public void failure(AbstractAuthenticationFailureEvent event) {
        LOG.warn("security_event=authentication_failure actor={}", actor(event.getAuthentication().getName()));
    }
}
