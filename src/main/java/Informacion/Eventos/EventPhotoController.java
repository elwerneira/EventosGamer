package Informacion.Eventos;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class EventPhotoController {
    @GetMapping({"/eventos/fotos/{filename}", "/api/eventos/fotos/{filename}"})
    ResponseEntity<Resource> photo(@PathVariable String filename) {
        if (!filename.matches("[a-z0-9-]+\\.jpg")) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        var resource = new ClassPathResource("event-photos/" + filename);
        if (!resource.exists()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG)
            .cacheControl(CacheControl.noStore()).body(resource);
    }
}
