<?php
declare(strict_types=1);

header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/DataBase.php';

try {
  if (!isset($pdo) || !($pdo instanceof PDO)) {
    throw new Exception('No existe $pdo en DataBase.php (conexión a BD).');
  }

  $idUsuario = $_GET['id_usuario'] ?? null;
  $estado = isset($_GET['estado']) ? trim((string)$_GET['estado']) : '';

  if (!is_int($idUsuario)) {
    if (is_string($idUsuario) && ctype_digit($idUsuario)) $idUsuario = (int)$idUsuario;
  }
  if (!is_int($idUsuario) || $idUsuario <= 0) {
    http_response_code(422);
    echo json_encode(['ok' => false, 'error' => 'id_usuario es obligatorio.'], JSON_UNESCAPED_UNICODE);
    exit;
  }

  $estadosPermitidos = ['pendiente', 'en_progreso', 'hecha'];

  $sql = "SELECT id_tarea, id_etiqueta, titulo, descripcion, prioridad, estado,
                 fecha_vencimiento, latitud, longitud, direccion, fecha_creacion
          FROM tareas
          WHERE id_usuario = :id_usuario";

  $params = [':id_usuario' => $idUsuario];

  if ($estado !== '' && in_array($estado, $estadosPermitidos, true)) {
    $sql .= " AND estado = :estado";
    $params[':estado'] = $estado;
  }

  $sql .= " ORDER BY fecha_creacion DESC";

  $stmt = $pdo->prepare($sql);
  $stmt->execute($params);

  $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);

  echo json_encode(['ok' => true, 'data' => $rows], JSON_UNESCAPED_UNICODE);

} catch (Throwable $e) {
  http_response_code(500);
  echo json_encode(['ok' => false, 'error' => 'Error al leer tareas.', 'detail' => $e->getMessage()], JSON_UNESCAPED_UNICODE);
}
