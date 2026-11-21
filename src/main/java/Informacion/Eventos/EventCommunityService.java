package Informacion.Eventos;

import jakarta.validation.constraints.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EventCommunityService {
    public record CommentForm(@NotBlank @Size(max = 500) String body) {}
    public record RatingForm(@NotNull @Min(1) @Max(5) Integer score) {}
    public record CommentView(String id, String author, String body, LocalDateTime createdAt) {}
    public record Community(EventSummary event, boolean canManage, List<EventMediaRepository.Info> media,
            List<CommentView> comments, long commentCount, double average, long votes, Integer myRating,
            boolean shared, Map<String, String> shareLinks) {}

    private final EventRepository events;
    private final EventOwnershipRepository owners;
    private final EventOwnershipService ownership;
    private final EventCommentRepository comments;
    private final EventRatingRepository ratings;
    private final EventMediaRepository media;
    private final EventShareRepository shares;
    private final AccountRepository accounts;
    private final String publicBase;

    public EventCommunityService(EventRepository events, EventOwnershipRepository owners,
            EventOwnershipService ownership, EventCommentRepository comments, EventRatingRepository ratings,
            EventMediaRepository media, EventShareRepository shares, AccountRepository accounts,
            @Value("${eventos.public-base-url:http://localhost:8080}") String publicBase) {
        this.events = events; this.owners = owners; this.ownership = ownership; this.comments = comments;
        this.ratings = ratings; this.media = media; this.shares = shares; this.accounts = accounts;
        var base = URI.create(publicBase);
        if (!("http".equals(base.getScheme()) || "https".equals(base.getScheme())) || base.getHost() == null
                || base.getUserInfo() != null || base.getQuery() != null || base.getFragment() != null
                || !(base.getPath().isEmpty() || "/".equals(base.getPath()))) {
            throw new IllegalArgumentException("eventos.public-base-url debe ser el origen http o https del sitio.");
        }
        this.publicBase = publicBase.endsWith("/") ? publicBase.substring(0, publicBase.length() - 1) : publicBase;
    }

    @Transactional(readOnly = true)
    public Community view(long eventId, String email) {
        Event event = requireEvent(eventId);
        boolean canManage = ownership.isOwner(eventId, email);
        Double average = ratings.average(eventId);
        var recent = comments.findTop50ByEventIdOrderByCreatedAtDesc(eventId).stream()
            .map(comment -> new CommentView(comment.id,
                accounts.findById(comment.authorEmail).map(account -> account.displayName).orElse("Jugador"),
                comment.body, comment.createdAt)).toList();
        return new Community(event.summary(), canManage, media.findByEventIdOrderByCreatedAtDesc(eventId),
            recent, comments.countByEventId(eventId), average == null ? 0 : average, ratings.countByEventId(eventId),
            ratings.findByEventIdAndAuthorEmail(eventId, email).map(rating -> rating.score).orElse(null),
            shares.existsById(eventId), canManage ? links(event) : Map.of());
    }

    @Transactional
    public void comment(long eventId, String email, CommentForm form) {
        requireEvent(eventId);
        var comment = new EventComment();
        comment.id = UUID.randomUUID().toString(); comment.eventId = eventId; comment.authorEmail = email;
        comment.body = form.body().trim(); comment.createdAt = LocalDateTime.now();
        comments.saveAndFlush(comment);
    }

    @Transactional
    public void rate(long eventId, String email, RatingForm form) {
        requireEvent(eventId);
        var rating = ratings.findByEventIdAndAuthorEmail(eventId, email).orElseGet(() -> {
            var item = new EventRating(); item.id = UUID.randomUUID().toString();
            item.eventId = eventId; item.authorEmail = email; return item;
        });
        rating.score = form.score();
        ratings.saveAndFlush(rating);
    }

    @Transactional
    public Map<String, String> share(long eventId, String email) {
        ownership.requireOwner(eventId, email);
        Event event = requireEvent(eventId);
        if (!shares.existsById(eventId)) {
            var share = new EventShare(); share.eventId = eventId; share.sharedAt = LocalDateTime.now();
            shares.saveAndFlush(share);
        }
        return links(event);
    }

    @Transactional(readOnly = true)
    public List<EventSummary> mine(String email) {
        return owners.findByAuthorEmail(email).stream().map(owner -> requireEvent(owner.eventId).summary()).toList();
    }

    @Transactional(readOnly = true)
    public List<EventSummary> shared() {
        return shares.findAllByOrderBySharedAtDesc().stream().map(share -> requireEvent(share.eventId).summary()).toList();
    }

    private Event requireEvent(long eventId) {
        return events.find(eventId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private Map<String, String> links(Event event) {
        String url = publicBase + "/eventos/" + event.id();
        String encoded = URLEncoder.encode(url, StandardCharsets.UTF_8);
        String message = URLEncoder.encode(event.title() + " " + url, StandardCharsets.UTF_8);
        return Map.of("url", url, "whatsapp", "https://wa.me/?text=" + message,
            "facebook", "https://www.facebook.com/sharer/sharer.php?u=" + encoded,
            "x", "https://twitter.com/intent/tweet?text=" + message);
    }
}
