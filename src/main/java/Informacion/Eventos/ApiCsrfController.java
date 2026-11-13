package Informacion.Eventos;

import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiCsrfController {
    @GetMapping("/api/csrf")
    ResponseEntity<Map<String, String>> token(CsrfToken csrf) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
            .body(Map.of("token", csrf.getToken(), "headerName", csrf.getHeaderName()));
    }
}
