CREATE DATABASE IF NOT EXISTS consulta_paises
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE consulta_paises;

CREATE TABLE IF NOT EXISTS search_history (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    search_term VARCHAR(120) NOT NULL,
    country_name VARCHAR(120) NOT NULL,
    country_code CHAR(2) NOT NULL,
    searched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
