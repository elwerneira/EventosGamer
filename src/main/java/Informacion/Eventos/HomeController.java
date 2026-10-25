package Informacion.Eventos;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final EventRepository events;

    public HomeController(EventRepository events) {
        this.events = events;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        var upcoming = events.search(null, null, null);
        model.addAttribute("events", upcoming);
        model.addAttribute("featured", upcoming.stream().filter(Event::featured).toList());
        return "Home";
    }
}
