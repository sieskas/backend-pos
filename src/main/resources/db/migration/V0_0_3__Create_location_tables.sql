-- V0_0_3__Create_location_tables.sql

CREATE TABLE location_types (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                name VARCHAR(50) UNIQUE NOT NULL
);

-- Table principale des locations (inchangé)
CREATE TABLE locations (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           label VARCHAR(255) NOT NULL,
                           type_id BIGINT NOT NULL,
                           parent_id BIGINT,
                           FOREIGN KEY (type_id) REFERENCES location_types(id),
                           FOREIGN KEY (parent_id) REFERENCES locations(id)
);

ALTER TABLE users ADD COLUMN root_location_id BIGINT,
    ADD FOREIGN KEY (root_location_id) REFERENCES locations(id);

-- Insertion des types de base (inchangé)
INSERT INTO location_types (id, name) VALUES
                                          (1, 'CHAIN'),
                                          (2, 'REGION'),
                                          (3, 'DISTRICT'),
                                          (4, 'STORE');