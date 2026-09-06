<?php

require_once __DIR__ . '/response.php';
require_once __DIR__ . '/db.php';
require_once __DIR__ . '/country_service.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    sendJson(false, 'Método no permitido.', null, 405);
}

$searchTerm = trim($_GET['name'] ?? '');
if ($searchTerm === '') {
    sendJson(false, 'Escribe el nombre de un país.', null, 422);
}

try {
    $country = fetchCountry($searchTerm);
    $statement = database()->prepare(
        'INSERT INTO search_history (search_term, country_name, country_code) VALUES (?, ?, ?)'
    );
    $statement->execute([$searchTerm, $country['name'], $country['code']]);
    sendJson(true, 'País encontrado.', $country);
} catch (RuntimeException $exception) {
    sendJson(false, $exception->getMessage(), null, 404);
} catch (Throwable $exception) {
    sendJson(false, 'No se pudo completar la consulta.', null, 500);
}
