<?php

$localConfig = __DIR__ . '/config.local.php';
return require file_exists($localConfig) ? $localConfig : __DIR__ . '/config.example.php';
