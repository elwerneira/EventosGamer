package Informacion.Eventos;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Comparator;

@Controller
public class HomeController {
    private final EventRepository events;

    public HomeController(EventRepository events) {
        this.events = events;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        var upcoming = events.search(null, null, null);
        model.addAttribute("events", upcoming.stream()
            .sorted(Comparator.comparing(Event::publishedAt, Comparator.nullsLast(Comparator.reverseOrder())))
            .map(Event::summary).toList());
        model.addAttribute("featured", upcoming.stream().filter(Event::popular).map(Event::summary).toList());
        return "Home";
    }
}
