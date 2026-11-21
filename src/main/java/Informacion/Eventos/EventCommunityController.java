package Informacion.Eventos;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class EventCommunityController {
    private final EventCommunityService community;
    private final EventMediaService media;

    public EventCommunityController(EventCommunityService community, EventMediaService media) {
        this.community = community; this.media = media;
    }

    @GetMapping("/eventos/{id}/comunidad")
    String detail(@PathVariable long id, Authentication authentication, Model model) {
        model.addAttribute("community", community.view(id, authentication.getName()));
        return "ComunidadEvento";
    }

    @GetMapping("/mis-eventos")
    String mine(Authentication authentication, Model model) {
        model.addAttribute("events", community.mine(authentication.getName()));
        model.addAttribute("heading", "Mis eventos");
        return "Comunidad";
    }

    @GetMapping("/comunidad")
    String shared(Model model) {
        model.addAttribute("events", community.shared());
        model.addAttribute("heading", "Compartido por la comunidad");
        return "Comunidad";
    }

    @PostMapping("/eventos/{id}/comentarios")
    String comment(@PathVariable long id, @Valid @ModelAttribute EventCommunityService.CommentForm form,
            BindingResult errors, Authentication authentication, RedirectAttributes flash) {
        if (errors.hasErrors()) flash.addFlashAttribute("error", "Escribe un comentario de 1 a 500 caracteres.");
        else community.comment(id, authentication.getName(), form);
        return redirect(id);
    }

    @PostMapping("/eventos/{id}/valoracion")
    String rate(@PathVariable long id, @Valid @ModelAttribute EventCommunityService.RatingForm form,
            BindingResult errors, Authentication authentication, RedirectAttributes flash) {
        if (errors.hasErrors()) flash.addFlashAttribute("error", "Selecciona una valoración de 1 a 5.");
        else community.rate(id, authentication.getName(), form);
        return redirect(id);
    }

    @PostMapping("/eventos/{id}/compartir")
    String share(@PathVariable long id, Authentication authentication, RedirectAttributes flash) {
        community.share(id, authentication.getName());
        flash.addFlashAttribute("message", "Tu evento ya aparece en la comunidad. También puedes compartir su enlace en redes.");
        return redirect(id);
    }

    @PostMapping("/eventos/{id}/media")
    String upload(@PathVariable long id, Authentication authentication, @RequestParam MultipartFile file,
            @RequestParam(defaultValue = "") String caption) {
        media.upload(id, authentication.getName(), file, caption);
        return redirect(id);
    }

    @GetMapping("/eventos/{id}/media/{mediaId}")
    @ResponseBody
    ResponseEntity<byte[]> download(@PathVariable long id, @PathVariable String mediaId) {
        return media.download(id, mediaId);
    }

    private String redirect(long id) { return "redirect:/eventos/" + id + "/comunidad"; }
}
