package Informacion.Eventos;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class EventController {
    private final EventRepository events;

    public EventController(EventRepository events) {
        this.events = events;
    }

    @GetMapping("/eventos")
    public String list(@RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String location, Model model) {
        category = category == null || category.isBlank() ? null : category;
        location = location == null || location.isBlank() ? null : location;
        model.addAttribute("events", events.search(category, date, location));
        model.addAttribute("categories", events.categories());
        model.addAttribute("locations", events.locations());
        model.addAttribute("category", category);
        model.addAttribute("date", date);
        model.addAttribute("location", location);
        return "Eventos";
    }

    @GetMapping("/eventos/{id}")
    public String detail(@PathVariable long id, Model model) {
        model.addAttribute("event", events.find(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
        return "Detalle";
    }
}
