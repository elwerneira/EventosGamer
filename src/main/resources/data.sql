INSERT INTO events (id, title, category, location, starts_at, description, address, organizers, parking, transport, food, accommodation, exhibitors, featured)
SELECT 1, 'Nexo Arena', 'Esports', 'Santiago', TIMESTAMP '2026-11-14 10:00:00',
    'Una jornada de torneos de Valorant y League of Legends, partidas amistosas y encuentros con la comunidad. Evento ficticio para demostrar la aplicación.',
    'Av. del Encuentro 120, Santiago (dirección de ejemplo)', 'Comunidad Nexo',
    'Estacionamiento de pago en el recinto.', 'Acceso mediante metro y buses urbanos.',
    'Patio de comidas con opciones vegetarianas.', 'Alojamiento por cuenta del asistente.',
    'Equipos locales, narradores y creadores de contenido de ejemplo.', TRUE
WHERE NOT EXISTS (SELECT 1 FROM events WHERE id = 1);

INSERT INTO events (id, title, category, location, starts_at, description, address, organizers, parking, transport, food, accommodation, exhibitors, featured)
SELECT 2, 'Pixel Independiente', 'Indie', 'Valparaíso', TIMESTAMP '2026-11-21 11:00:00',
    'Descubre prototipos, conversa con desarrolladores y participa en talleres de creación de videojuegos. Evento ficticio para demostrar la aplicación.',
    'Pasaje Pixel 45, Valparaíso (dirección de ejemplo)', 'Colectivo Pixel',
    'Sin estacionamiento propio; usar recintos cercanos.', 'Buses urbanos con paradas cercanas.',
    'Cafetería y puestos de comida.', 'Hostales del sector, sin convenio con el evento.',
    'Estudios indie y artistas digitales de ejemplo.', TRUE
WHERE NOT EXISTS (SELECT 1 FROM events WHERE id = 2);

INSERT INTO events (id, title, category, location, starts_at, description, address, organizers, parking, transport, food, accommodation, exhibitors, featured)
SELECT 3, 'Checkpoint Retro', 'Retro', 'Concepción', TIMESTAMP '2026-12-05 12:00:00',
    'Consolas clásicas, arcade y torneos amistosos para compartir entre generaciones. Evento ficticio para demostrar la aplicación.',
    'Calle Checkpoint 88, Concepción (dirección de ejemplo)', 'Club Checkpoint',
    'Cupos limitados en el recinto.', 'Acceso en buses urbanos.',
    'Puestos de comida y zona de descanso.', 'Hoteles del centro, reserva independiente.',
    'Coleccionistas y restauradores de consolas de ejemplo.', FALSE
WHERE NOT EXISTS (SELECT 1 FROM events WHERE id = 3);
