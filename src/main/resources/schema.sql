CREATE TABLE IF NOT EXISTS accounts (
    email VARCHAR(254) PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    display_name VARCHAR(40) NOT NULL,
    games VARCHAR(160) NOT NULL DEFAULT '',
    avatar VARCHAR(20) NOT NULL DEFAULT 'explorador',
    notifications BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS events (
    id BIGINT PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    category VARCHAR(60) NOT NULL,
    location VARCHAR(80) NOT NULL,
    starts_at TIMESTAMP NOT NULL,
    description VARCHAR(1500) NOT NULL,
    address VARCHAR(200) NOT NULL,
    organizers VARCHAR(200) NOT NULL,
    parking VARCHAR(200) NOT NULL,
    transport VARCHAR(200) NOT NULL,
    food VARCHAR(200) NOT NULL,
    accommodation VARCHAR(200) NOT NULL,
    exhibitors VARCHAR(300) NOT NULL,
    featured BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE TABLE IF NOT EXISTS account_roles (
    email VARCHAR(254) PRIMARY KEY,
    role VARCHAR(10) NOT NULL
);

CREATE TABLE IF NOT EXISTS event_details (
    event_id BIGINT PRIMARY KEY,
    video_game VARCHAR(100) NOT NULL,
    country VARCHAR(80) NOT NULL,
    modality VARCHAR(15) NOT NULL,
    access_url VARCHAR(300) NOT NULL,
    requirements VARCHAR(1000) NOT NULL,
    registration_info VARCHAR(1000) NOT NULL,
    published_at TIMESTAMP NOT NULL,
    popular BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (event_id) REFERENCES events(id)
);

CREATE TABLE IF NOT EXISTS event_photos (
    event_id BIGINT NOT NULL,
    photo_order INTEGER NOT NULL,
    filename VARCHAR(80) NOT NULL,
    alt VARCHAR(200) NOT NULL,
    credit VARCHAR(200) NOT NULL,
    source_url VARCHAR(400) NOT NULL,
    license_url VARCHAR(120) NOT NULL,
    PRIMARY KEY (event_id, photo_order),
    FOREIGN KEY (event_id) REFERENCES event_details(event_id)
);
