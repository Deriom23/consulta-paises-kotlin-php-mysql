<?php

function normalizeWorldBankCountry(string $catalogJson, string $populationJson, string $searchTerm): array
{
    $catalog = json_decode($catalogJson, true, 512, JSON_THROW_ON_ERROR);
    $items = $catalog[1] ?? [];
    $needle = mb_strtolower(trim($searchTerm));
    $country = null;
    foreach ($items as $item) {
        if (mb_strtolower($item['name'] ?? '') === $needle) { $country = $item; break; }
    }
    if ($country === null) {
        foreach ($items as $item) {
            if (str_contains(mb_strtolower($item['name'] ?? ''), $needle)) { $country = $item; break; }
        }
    }
    if ($country === null || empty($country['iso2Code']) || ($country['region']['id'] ?? '') === 'NA') {
        throw new RuntimeException('No se encontró el país solicitado.');
    }
    $populationData = json_decode($populationJson, true, 512, JSON_THROW_ON_ERROR);
    $iso2 = strtoupper($country['iso2Code']);
    return [
        'name' => $country['name'], 'code' => $iso2,
        'capital' => $country['capitalCity'] ?: 'No disponible',
        'continent' => $country['region']['value'] ?? 'No disponible',
        'population' => (int) ($populationData[1][0]['value'] ?? 0),
        'income_level' => $country['incomeLevel']['value'] ?? 'No disponible',
        'coordinates' => ($country['latitude'] ?: '?') . ', ' . ($country['longitude'] ?: '?'),
        'flag_url' => 'https://flagcdn.com/w320/' . strtolower($iso2) . '.png'
    ];
}

function fetchWorldBankJson(string $url): string
{
    $context = stream_context_create([
        'http' => ['timeout' => 15, 'ignore_errors' => true, 'header' => "User-Agent: ConsultaPaisesCurso/1.0\r\n"],
        'ssl' => ['verify_peer' => true, 'verify_peer_name' => true]
    ]);
    $json = @file_get_contents($url, false, $context);
    $lastStatus = '';
    foreach (($http_response_header ?? []) as $header) {
        if (str_starts_with($header, 'HTTP/')) $lastStatus = $header;
    }
    if ($json === false || !str_contains($lastStatus, '200')) {
        throw new RuntimeException('La API extranjera no está disponible.');
    }
    return $json;
}

function fetchCountry(string $countryName): array
{
    $catalog = fetchWorldBankJson('https://api.worldbank.org/v2/country?format=json&per_page=400');
    $decoded = json_decode($catalog, true, 512, JSON_THROW_ON_ERROR);
    $needle = mb_strtolower(trim($countryName));
    $found = null;
    foreach (($decoded[1] ?? []) as $item) {
        $candidate = mb_strtolower($item['name'] ?? '');
        if ($candidate === $needle || str_contains($candidate, $needle)) { $found = $item; break; }
    }
    if ($found === null || empty($found['id']) || ($found['region']['id'] ?? '') === 'NA') {
        throw new RuntimeException('No se encontró el país solicitado.');
    }
    $population = fetchWorldBankJson('https://api.worldbank.org/v2/country/' . rawurlencode($found['id']) . '/indicator/SP.POP.TOTL?format=json&mrnev=1');
    return normalizeWorldBankCountry($catalog, $population, $countryName);
}
