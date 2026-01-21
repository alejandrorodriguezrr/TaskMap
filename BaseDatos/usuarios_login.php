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

$correo = isset($data['correo']) ? trim((string)$data['correo']) : '';
$contrasena = isset($data['contrasena']) ? (string)$data['contrasena'] : '';

if ($correo === '' || $contrasena === '') {
  http_response_code(422);
  echo json_encode(['ok' => false, 'error' => 'correo y contrasena son obligatorios.'], JSON_UNESCAPED_UNICODE);
  exit;
}

try {
  $sql = "SELECT id_usuario, nombre, contrasena_hash
          FROM usuarios
          WHERE correo = :correo
          LIMIT 1";
  $stmt = $pdo->prepare($sql);
  $stmt->execute([':correo' => $correo]);
  $usuario = $stmt->fetch();

  if (!$usuario) {
    http_response_code(401);
    echo json_encode(['ok' => false, 'error' => 'Credenciales incorrectas.'], JSON_UNESCAPED_UNICODE);
    exit;
  }

  $hash = (string)$usuario['contrasena_hash'];
  if (!password_verify($contrasena, $hash)) {
    http_response_code(401);
    echo json_encode(['ok' => false, 'error' => 'Credenciales incorrectas.'], JSON_UNESCAPED_UNICODE);
    exit;
  }

  echo json_encode([
    'ok' => true,
    'id_usuario' => (int)$usuario['id_usuario'],
    'nombre' => $usuario['nombre']
  ], JSON_UNESCAPED_UNICODE);

} catch (Throwable $e) {
  http_response_code(500);
  echo json_encode(['ok' => false, 'error' => 'Error interno en login.', 'detail' => $e->getMessage()], JSON_UNESCAPED_UNICODE);
}
