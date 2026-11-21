package Informacion.Eventos;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class EventCommunityApiController {
    private final EventCommunityService community;
    private final EventMediaService media;

    public EventCommunityApiController(EventCommunityService community, EventMediaService media) {
        this.community = community; this.media = media;
    }

    @GetMapping("/api/eventos/{id}/comunidad")
    EventCommunityService.Community detail(@PathVariable long id, Authentication authentication) {
        return community.view(id, authentication.getName());
    }

    @GetMapping("/api/mis-eventos")
    List<EventSummary> mine(Authentication authentication) { return community.mine(authentication.getName()); }

    @GetMapping("/api/comunidad")
    List<EventSummary> shared() { return community.shared(); }

    @PostMapping("/api/eventos/{id}/comentarios")
    ResponseEntity<Void> comment(@PathVariable long id, Authentication authentication,
            @Valid @RequestBody EventCommunityService.CommentForm form) {
        community.comment(id, authentication.getName(), form);
        return ResponseEntity.created(URI.create("/api/eventos/" + id + "/comunidad")).build();
    }

    @PutMapping("/api/eventos/{id}/valoracion")
    ResponseEntity<Void> rate(@PathVariable long id, Authentication authentication,
            @Valid @RequestBody EventCommunityService.RatingForm form) {
        community.rate(id, authentication.getName(), form);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/eventos/{id}/compartir")
    Map<String, String> share(@PathVariable long id, Authentication authentication) {
        return community.share(id, authentication.getName());
    }

    @PostMapping(value = "/api/eventos/{id}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<Map<String, String>> upload(@PathVariable long id, Authentication authentication,
            @RequestParam MultipartFile file, @RequestParam(defaultValue = "") String caption) {
        String mediaId = media.upload(id, authentication.getName(), file, caption);
        return ResponseEntity.created(URI.create("/api/eventos/" + id + "/media/" + mediaId))
            .body(Map.of("id", mediaId));
    }

    @GetMapping("/api/eventos/{id}/media/{mediaId}")
    ResponseEntity<byte[]> download(@PathVariable long id, @PathVariable String mediaId) {
        return media.download(id, mediaId);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> invalid() {
        return ResponseEntity.badRequest().body(Map.of("error", "invalid_request"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<Map<String, String>> tooLarge() {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Map.of("error", "media_too_large"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Map<String, String>> conflict() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "concurrent_update_retry"));
    }
}
