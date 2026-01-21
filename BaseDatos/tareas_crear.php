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

$idUsuario = $data['id_usuario'] ?? null;
$titulo = isset($data['titulo']) ? trim((string)$data['titulo']) : '';
$descripcion = array_key_exists('descripcion', $data) ? (string)$data['descripcion'] : null;

$prioridad = isset($data['prioridad']) ? trim((string)$data['prioridad']) : 'media';
$estado = isset($data['estado']) ? trim((string)$data['estado']) : 'pendiente';
$fechaVencimiento = array_key_exists('fecha_vencimiento', $data) ? $data['fecha_vencimiento'] : null;

$idEtiqueta = array_key_exists('id_etiqueta', $data) ? $data['id_etiqueta'] : null;

$latitud = array_key_exists('latitud', $data) ? $data['latitud'] : null;
$longitud = array_key_exists('longitud', $data) ? $data['longitud'] : null;
$direccion = array_key_exists('direccion', $data) ? $data['direccion'] : null;

if (!is_int($idUsuario)) {
  if (is_string($idUsuario) && ctype_digit($idUsuario)) $idUsuario = (int)$idUsuario;
}
if (!is_int($idUsuario) || $idUsuario <= 0) {
  http_response_code(422);
  echo json_encode(['ok' => false, 'error' => 'id_usuario es obligatorio.'], JSON_UNESCAPED_UNICODE);
  exit;
}

if ($titulo === '' || mb_strlen($titulo) > 120) {
  http_response_code(422);
  echo json_encode(['ok' => false, 'error' => 'titulo es obligatorio y máximo 120 caracteres.'], JSON_UNESCAPED_UNICODE);
  exit;
}

$prioridades = ['baja', 'media', 'alta'];
$estados = ['pendiente', 'en_progreso', 'hecha'];

if (!in_array($prioridad, $prioridades, true)) $prioridad = 'media';
if (!in_array($estado, $estados, true)) $estado = 'pendiente';

try {
  $sql = "INSERT INTO tareas
          (id_usuario, id_etiqueta, titulo, descripcion, prioridad, estado, fecha_vencimiento, latitud, longitud, direccion)
          VALUES
          (:id_usuario, :id_etiqueta, :titulo, :descripcion, :prioridad, :estado, :fecha_vencimiento, :latitud, :longitud, :direccion)";

  $stmt = $pdo->prepare($sql);
  $stmt->execute([
    ':id_usuario' => $idUsuario,
    ':id_etiqueta' => $idEtiqueta,
    ':titulo' => $titulo,
    ':descripcion' => $descripcion,
    ':prioridad' => $prioridad,
    ':estado' => $estado,
    ':fecha_vencimiento' => $fechaVencimiento,
    ':latitud' => $latitud,
    ':longitud' => $longitud,
    ':direccion' => $direccion
  ]);

  echo json_encode(['ok' => true, 'id_tarea' => (int)$pdo->lastInsertId()], JSON_UNESCAPED_UNICODE);

} catch (Throwable $e) {
  http_response_code(500);
  echo json_encode(['ok' => false, 'error' => 'Error interno al crear tarea.', 'detail' => $e->getMessage()], JSON_UNESCAPED_UNICODE);
}
