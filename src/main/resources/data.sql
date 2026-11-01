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

INSERT INTO events (id, title, category, location, starts_at, description, address, organizers, parking, transport, food, accommodation, exhibitors, featured)
SELECT 4, 'GREEGAME Online', 'Comunidad', 'Buenos Aires', TIMESTAMP '2026-12-12 18:00:00',
    'Construcciones colaborativas y desafíos de Minecraft desde casa. Evento ficticio para demostrar la modalidad online.',
    'No aplica: evento online.', 'Comunidad GREEGAME de demostración',
    'No aplica.', 'No aplica.', 'No aplica.', 'No aplica.', 'Creadores de mundos de ejemplo.', FALSE
WHERE NOT EXISTS (SELECT 1 FROM events WHERE id = 4);

INSERT INTO event_details (event_id, video_game, country, modality, access_url, requirements, registration_info, published_at, popular)
SELECT 1, 'Valorant', 'Chile', 'Presencial', '',
    'Edad mínima de 16 años. Presentar identificación y llevar cuenta propia de Valorant. Los menores asisten con una persona adulta.',
    'Inscripción gratuita en la recepción del recinto desde las 09:00; cupos limitados por orden de llegada. Información ficticia de demostración.',
    TIMESTAMP '2026-10-28 12:00:00', TRUE
WHERE EXISTS (SELECT 1 FROM events WHERE id = 1) AND NOT EXISTS (SELECT 1 FROM event_details WHERE event_id = 1);

INSERT INTO event_details (event_id, video_game, country, modality, access_url, requirements, registration_info, published_at, popular)
SELECT 2, 'Hollow Knight', 'Chile', 'Presencial', '',
    'Actividad para mayores de 12 años; menores acompañados por una persona adulta. No se requiere experiencia previa.',
    'Inscripción en la recepción del evento desde las 10:30. Talleres sujetos a cupos. Información ficticia de demostración.',
    TIMESTAMP '2026-10-29 12:00:00', TRUE
WHERE EXISTS (SELECT 1 FROM events WHERE id = 2) AND NOT EXISTS (SELECT 1 FROM event_details WHERE event_id = 2);

INSERT INTO event_details (event_id, video_game, country, modality, access_url, requirements, registration_info, published_at, popular)
SELECT 3, 'Street Fighter II', 'Chile', 'Presencial', '',
    'Todas las edades; menores acompañados por una persona adulta. Respetar los turnos y cuidar las consolas compartidas.',
    'Entrada libre; anotación para los torneos en el recinto desde las 11:30. Información ficticia de demostración.',
    TIMESTAMP '2026-10-30 12:00:00', FALSE
WHERE EXISTS (SELECT 1 FROM events WHERE id = 3) AND NOT EXISTS (SELECT 1 FROM event_details WHERE event_id = 3);

INSERT INTO event_details (event_id, video_game, country, modality, access_url, requirements, registration_info, published_at, popular)
SELECT 4, 'Minecraft', 'Argentina', 'Online', 'https://example.com/greegame-online',
    'Minecraft Java Edition actualizado, conexión estable y cuenta propia. Edad mínima de 13 años. Horario local del organizador en Buenos Aires.',
    'Ejemplo académico sin inscripción real: el organizador confirmaría el cupo y enviaría las instrucciones de acceso. El enlace mostrado es de demostración.',
    TIMESTAMP '2026-10-31 12:00:00', FALSE
WHERE EXISTS (SELECT 1 FROM events WHERE id = 4) AND NOT EXISTS (SELECT 1 FROM event_details WHERE event_id = 4);

INSERT INTO event_photos (event_id, photo_order, filename, alt, credit, source_url, license_url)
SELECT d.event_id, 0, 'torneo-esports.jpg', 'Participantes de un torneo de videojuegos en Camp Mabry en 2019; fotografía ilustrativa.',
    'Brittany Nelson / U.S. Army · CC BY 2.0 · Copia sin edición',
    'https://commons.wikimedia.org/wiki/File:2019_Road_to_TwithCon_Esports_tournament_Camp_Mabry_(32864490237).jpg',
    'https://creativecommons.org/licenses/by/2.0/'
FROM event_details d WHERE d.event_id IN (1, 2, 3, 4)
AND NOT EXISTS (SELECT 1 FROM event_photos p WHERE p.event_id = d.event_id AND p.photo_order = 0);

INSERT INTO event_photos (event_id, photo_order, filename, alt, credit, source_url, license_url)
SELECT d.event_id, 1, 'encuentro-retro.jpg', 'Zona de juegos retro en Japan Expo 2012; fotografía ilustrativa.',
    'Yves Tennevin · CC BY-SA 3.0 · Copia sin edición',
    'https://commons.wikimedia.org/wiki/File:Japan_Expo_13_-_Retro_Gaming_-_2012-0706-_P1410070.jpg',
    'https://creativecommons.org/licenses/by-sa/3.0/'
FROM event_details d WHERE d.event_id IN (1, 2, 3, 4)
AND NOT EXISTS (SELECT 1 FROM event_photos p WHERE p.event_id = d.event_id AND p.photo_order = 1);
