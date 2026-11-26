package Informacion.Eventos;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import java.time.*;
import jakarta.persistence.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.mock.web.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.BeanPropertyBindingResult;

class SecurityAuditTest {
@Test void auditRemovesControlCharactersAndLimitsActorLength() {
 var logger=(ch.qos.logback.classic.Logger)org.slf4j.LoggerFactory.getLogger(SecurityAudit.class);
 var appender=new ch.qos.logback.core.read.ListAppender<ch.qos.logback.classic.spi.ILoggingEvent>();
 appender.start(); logger.addAppender(appender);
 try {
 var audit=new SecurityAudit();
 var auth=new UsernamePasswordAuthenticationToken("evil\r\n"+"x".repeat(100),null);
 audit.success(new org.springframework.security.authentication.event.AuthenticationSuccessEvent(auth));
 audit.failure(new org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent(auth,new org.springframework.security.authentication.BadCredentialsException("invalid")));
 assertThat(appender.list).hasSize(2);
 assertThat(appender.list.get(0).getFormattedMessage()).doesNotContain("\r","\n");
 assertThat(appender.list.get(1).getFormattedMessage()).contains("authentication_failure");
 } finally {logger.detachAppender(appender);appender.stop();}
}
}
