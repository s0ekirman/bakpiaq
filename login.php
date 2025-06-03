<?php
include 'koneksi.php';

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

// Hanya terima method POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode([
        'status' => 'error',
        'message' => 'Method tidak diizinkan'
    ]);
    exit;
}

try {
    // Ambil data dari POST
    $input = json_decode(file_get_contents('php://input'), true);
    
    // Jika tidak ada input JSON, coba dari form data
    if (!$input) {
        $username_email = $_POST['username'] ?? '';
        $password = $_POST['password'] ?? '';
    } else {
        $username_email = $input['username'] ?? '';
        $password = $input['password'] ?? '';
    }
    
    // Validasi input
    if (empty($username_email) || empty($password)) {
        echo json_encode([
            'status' => 'error',
            'message' => 'Username/email dan password harus diisi'
        ]);
        exit;
    }
    
    // Query untuk mencari user berdasarkan username atau email
    $query = "SELECT urutan, id_akun, username, email, password, jabatan, dibuat_pada FROM user WHERE username = ? OR email = ?";
    $stmt = $conn->prepare($query);
    $stmt->bind_param("ss", $username_email, $username_email);
    $stmt->execute();
    $result = $stmt->get_result();
    
    if ($result->num_rows === 0) {
        echo json_encode([
            'status' => 'error',
            'message' => 'Username/email atau password salah'
        ]);
        exit;
    }
    
    $user = $result->fetch_assoc();
    
    // Verifikasi password
    if (!password_verify($password, $user['password'])) {
        echo json_encode([
            'status' => 'error',
            'message' => 'Username/email atau password salah'
        ]);
        exit;
    }
    
    // Pastikan jabatan adalah integer
    $jabatan = intval($user['jabatan']);
    
    // Tentukan redirect URL berdasarkan jabatan
    $redirect_url = '';
    switch ($jabatan) {
        case 1: // Admin
            $redirect_url = 'C:\\laragon\\www\\bakpiaqq\\atmin\\index.php';
            break;
        case 2: // Karyawan/Staff
            $redirect_url = 'C:\\laragon\\www\\bakpiaqq\\staff\\index.php';
            break;
        case 3: // Pengguna
            $redirect_url = 'C:\\laragon\\www\\bakpiaqq\\user\\index.php';
            break;
        default:
            $redirect_url = 'C:\\laragon\\www\\bakpiaqq\\user\\index.php';
    }
    
    // Response sukses
    echo json_encode([
        'status' => 'success',
        'message' => 'Login berhasil',
        'data' => [
            'urutan' => intval($user['urutan']),
            'id_akun' => $user['id_akun'],
            'username' => $user['username'],
            'email' => $user['email'],
            'jabatan' => $jabatan,
            'jabatan_nama' => $jabatan == 1 ? 'Admin' : ($jabatan == 2 ? 'Karyawan' : 'Pengguna'),
            'dibuat_pada' => $user['dibuat_pada'],
            'redirect_url' => $redirect_url
        ]
    ]);
    
    $stmt->close();
    
} catch (Exception $e) {
    echo json_encode([
        'status' => 'error',
        'message' => 'Terjadi kesalahan sistem: ' . $e->getMessage()
    ]);
}

$conn->close();
?>