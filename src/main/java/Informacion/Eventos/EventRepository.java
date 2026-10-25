package Informacion.Eventos;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class EventRepository {
    private final JdbcTemplate jdbc;
    private static final RowMapper<Event> MAPPER = (rs, row) -> new Event(rs.getLong("id"),
            rs.getString("title"), rs.getString("category"), rs.getString("location"),
            rs.getTimestamp("starts_at").toLocalDateTime(), rs.getString("description"),
            rs.getString("address"), rs.getString("organizers"), rs.getString("parking"),
            rs.getString("transport"), rs.getString("food"), rs.getString("accommodation"),
            rs.getString("exhibitors"), rs.getBoolean("featured"));

    public EventRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Event> search(String category, LocalDate date, String location) {
        return jdbc.query("""
                SELECT * FROM events WHERE starts_at >= CURRENT_TIMESTAMP
                AND (? IS NULL OR category = ?)
                AND (? IS NULL OR CAST(starts_at AS DATE) = ?)
                AND (? IS NULL OR location = ?)
                ORDER BY starts_at
                """, MAPPER, category, category, date, date, location, location);
    }

    public Optional<Event> find(long id) {
        return jdbc.query("SELECT * FROM events WHERE id = ?", MAPPER, id).stream().findFirst();
    }

    public List<String> categories() {
        return jdbc.queryForList("SELECT DISTINCT category FROM events ORDER BY category", String.class);
    }

    public List<String> locations() {
        return jdbc.queryForList("SELECT DISTINCT location FROM events ORDER BY location", String.class);
    }
}
