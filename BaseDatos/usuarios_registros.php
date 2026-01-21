<?php
declare(strict_types=1);

header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/DataBase.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
  http_response_code(405);
  echo json_encode(['ok' => false, 'error' => 'Método no permitido. Usa POST.'], JSON_UNESCAPED_UNICODE);
  exit;
}

$raw = file_get_contents('php://input');
$data = json_decode($raw ?? '', true);

if (!is_array($data)) {
  http_response_code(400);
  echo json_encode(['ok' => false, 'error' => 'JSON inválido.'], JSON_UNESCAPED_UNICODE);
  exit;
}

$nombre = isset($data['nombre']) ? trim((string)$data['nombre']) : null;
$correo = isset($data['correo']) ? trim((string)$data['correo']) : '';
$contrasena = isset($data['contrasena']) ? (string)$data['contrasena'] : '';

if ($correo === '' || mb_strlen($correo) > 180) {
  http_response_code(422);
  echo json_encode(['ok' => false, 'error' => 'correo es obligatorio y máximo 180 caracteres.'], JSON_UNESCAPED_UNICODE);
  exit;
}

if ($contrasena === '' || mb_strlen($contrasena) < 4) {
  http_response_code(422);
  echo json_encode(['ok' => false, 'error' => 'contrasena es obligatoria (mínimo 4 caracteres).'], JSON_UNESCAPED_UNICODE);
  exit;
}

if ($nombre !== null && $nombre !== '' && mb_strlen($nombre) > 80) {
  http_response_code(422);
  echo json_encode(['ok' => false, 'error' => 'nombre máximo 80 caracteres.'], JSON_UNESCAPED_UNICODE);
  exit;
}

$hash = password_hash($contrasena, PASSWORD_DEFAULT);

try {
  if (!isset($pdo) || !($pdo instanceof PDO)) {
    throw new Exception('No existe $pdo en DataBase.php.');
  }

  $sql = "INSERT INTO usuarios (nombre, correo, contrasena_hash)
          VALUES (:nombre, :correo, :hash)";

  $stmt = $pdo->prepare($sql);
  $stmt->execute([
    ':nombre' => ($nombre === '' ? null : $nombre),
    ':correo' => $correo,
    ':hash' => $hash
  ]);

  echo json_encode(['ok' => true, 'id_usuario' => (int)$pdo->lastInsertId()], JSON_UNESCAPED_UNICODE);

} catch (Throwable $e) {
  if ($e instanceof PDOException && (string)$e->getCode() === '23000') {
    http_response_code(409);
    echo json_encode(['ok' => false, 'error' => 'Ese correo ya existe.'], JSON_UNESCAPED_UNICODE);
    exit;
  }

  http_response_code(500);
  echo json_encode(['ok' => false, 'error' => 'Error interno al registrar.', 'detail' => $e->getMessage()], JSON_UNESCAPED_UNICODE);
}
