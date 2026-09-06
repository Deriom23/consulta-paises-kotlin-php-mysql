<?php

require_once __DIR__ . '/response.php';
require_once __DIR__ . '/db.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    sendJson(false, 'Método no permitido.', null, 405);
}

try {
    $items = database()->query(
        'SELECT id, search_term, country_name, country_code, '
        . "DATE_FORMAT(searched_at, '%d/%m/%Y %H:%i') AS searched_at "
        . 'FROM search_history ORDER BY id DESC LIMIT 100'
    )->fetchAll();
    sendJson(true, 'Historial obtenido.', $items);
} catch (Throwable $exception) {
    sendJson(false, 'No se pudo cargar el historial.', null, 500);
}
