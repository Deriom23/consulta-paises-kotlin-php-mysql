<?php

require_once __DIR__ . '/../country_service.php';

$catalog = json_encode([['page' => 1], [[
    'id' => 'PER', 'iso2Code' => 'PE', 'name' => 'Peru',
    'region' => ['value' => 'Latin America & Caribbean'],
    'incomeLevel' => ['value' => 'Upper middle income'],
    'capitalCity' => 'Lima', 'longitude' => '-77.0465', 'latitude' => '-12.0931'
]]]);
$population = json_encode([['page' => 1], [[
    'countryiso3code' => 'PER', 'date' => '2024', 'value' => 34217848
]]]);

$country = normalizeWorldBankCountry($catalog, $population, 'Peru');

assert($country['name'] === 'Peru');
assert($country['code'] === 'PE');
assert($country['capital'] === 'Lima');
assert($country['population'] === 34217848);
assert($country['income_level'] === 'Upper middle income');
assert($country['coordinates'] === '-12.0931, -77.0465');
assert($country['flag_url'] === 'https://flagcdn.com/w320/pe.png');

echo "country_service_test: OK\n";
