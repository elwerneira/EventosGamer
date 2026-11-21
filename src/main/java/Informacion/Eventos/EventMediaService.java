package Informacion.Eventos;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EventMediaService {
    private final EventMediaRepository media;
    private final EventOwnershipService ownership;

    public EventMediaService(EventMediaRepository media, EventOwnershipService ownership) {
        this.media = media; this.ownership = ownership;
    }

    @Transactional
    public String upload(long eventId, String email, MultipartFile file, String caption) {
        ownership.requireOwner(eventId, email);
        if (file.isEmpty() || file.getSize() > 7L * 1024 * 1024 || caption.length() > 240) {
            throw invalid();
        }
        try {
            byte[] content = file.getBytes();
            String type = file.getContentType();
            if ("image/jpeg".equals(type) || "image/png".equals(type)) {
                if (content.length > 5 * 1024 * 1024) throw invalid();
                validateImage(content, type);
            } else if ("video/mp4".equals(type)) {
                if (content.length < 16 || ByteBuffer.wrap(content).getInt() < 16
                        || ByteBuffer.wrap(content).getInt() > content.length
                        || !"ftyp".equals(new String(content, 4, 4, StandardCharsets.US_ASCII))) throw invalid();
            } else {
                throw invalid();
            }
            var item = new EventMedia();
            item.id = UUID.randomUUID().toString();
            item.eventId = eventId;
            item.kind = type.startsWith("image/") ? "PHOTO" : "VIDEO";
            item.contentType = type;
            item.caption = caption.trim();
            item.createdAt = LocalDateTime.now();
            item.content = content;
            media.saveAndFlush(item);
            return item.id;
        } catch (IOException ex) {
            throw invalid();
        }
    }

    private void validateImage(byte[] content, String type) throws IOException {
        try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(content))) {
            var readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw invalid();
            var reader = readers.next();
            try {
                reader.setInput(input);
                String format = reader.getFormatName();
                if (!("PNG".equalsIgnoreCase(format) && "image/png".equals(type))
                        && !("JPEG".equalsIgnoreCase(format) && "image/jpeg".equals(type))) throw invalid();
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (width < 1 || height < 1 || (long) width * height > 10_000_000) throw invalid();
                reader.read(0);
            } finally {
                reader.dispose();
            }
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> download(long eventId, String mediaId) {
        var item = media.findByIdAndEventId(mediaId, eventId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(item.contentType))
            .cacheControl(CacheControl.noStore()).body(item.content);
    }

    private ResponseStatusException invalid() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Usa JPEG o PNG de hasta 5 MB y 10 megapíxeles, o MP4 de hasta 7 MB. Descripción: hasta 240 caracteres.");
    }
}
