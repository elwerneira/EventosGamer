package Informacion.Eventos;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@Controller
public class EventPublicationController {
    private final EventPublicationService publications;

    public EventPublicationController(EventPublicationService publications) { this.publications = publications; }

    @GetMapping("/eventos/publicar")
    String form(Model model) {
        model.addAttribute("form", new EventPublicationForm("", "", "", null, "", "", "", "Presencial", "", "", "", "", ""));
        return "PublicarEvento";
    }

    @PostMapping("/eventos/publicar")
    String publish(@Valid @ModelAttribute("form") EventPublicationForm form, BindingResult errors) {
        if (errors.hasErrors()) return "PublicarEvento";
        return "redirect:/eventos/" + publications.publish(form).id();
    }

    @PostMapping("/api/eventos")
    @ResponseBody
    ResponseEntity<Event> publishApi(@Valid @RequestBody EventPublicationForm form) {
        Event event = publications.publish(form);
        return ResponseEntity.created(URI.create("/api/eventos/" + event.id())).body(event);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    ResponseEntity<Map<String, String>> invalidPublication() {
        return ResponseEntity.badRequest().body(Map.of("error", "invalid_event"));
    }
}
