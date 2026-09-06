<?php

require_once __DIR__ . '/response.php';
require_once __DIR__ . '/db.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendJson(false, 'Método no permitido.', null, 405);
}

try {
    $deleted = database()->exec('DELETE FROM search_history');
    sendJson(true, 'Historial eliminado.', ['deleted' => $deleted]);
} catch (Throwable $exception) {
    sendJson(false, 'No se pudo borrar el historial.', null, 500);
}
