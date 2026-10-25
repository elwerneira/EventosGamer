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
