<?php
declare(strict_types=1);

header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/DataBase.php';

try {
  if (!isset($pdo) || !($pdo instanceof PDO)) {
    throw new Exception('No existe $pdo en DataBase.php (conexión a BD).');
  }

  $idUsuario = $_GET['id_usuario'] ?? null;

  if (!is_int($idUsuario)) {
    if (is_string($idUsuario) && ctype_digit($idUsuario)) $idUsuario = (int)$idUsuario;
  }
  if (!is_int($idUsuario) || $idUsuario <= 0) {
    http_response_code(422);
    echo json_encode(['ok' => false, 'error' => 'id_usuario es obligatorio.'], JSON_UNESCAPED_UNICODE);
    exit;
  }

  $stmt = $pdo->prepare("SELECT id_etiqueta, nombre, fecha_creacion
                         FROM etiquetas
                         WHERE id_usuario = :id_usuario
                         ORDER BY nombre ASC");
  $stmt->execute([':id_usuario' => $idUsuario]);

  $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);

  echo json_encode(['ok' => true, 'data' => $rows], JSON_UNESCAPED_UNICODE);

} catch (Throwable $e) {
  http_response_code(500);
  echo json_encode(['ok' => false, 'error' => 'Error al leer etiquetas.', 'detail' => $e->getMessage()], JSON_UNESCAPED_UNICODE);
}
