<?php
include 'koneksi.php';

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
    $username = trim($_POST['username']);
    $email = trim($_POST['email']);
    $password = $_POST['password'];
    $jabatan = isset($_POST['jabatan']) ? intval($_POST['jabatan']) : 3; // Default pengguna
    
    // Validasi input
    if (empty($username) || empty($email) || empty($password)) {
        echo json_encode([
            'status' => 'error',
            'message' => 'Semua field harus diisi'
        ]);
        exit;
    }
    
    // Validasi panjang password
    if (strlen($password) < 8) {
        echo json_encode([
            'status' => 'error',
            'message' => 'Password minimal 8 karakter'
        ]);
        exit;
    }
    
    // Validasi email
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        echo json_encode([
            'status' => 'error',
            'message' => 'Format email tidak valid'
        ]);
        exit;
    }
    
    // Validasi jabatan
    if (!in_array($jabatan, [1, 2, 3])) {
        echo json_encode([
            'status' => 'error',
            'message' => 'Jabatan tidak valid'
        ]);
        exit;
    }
    
    // Cek apakah username atau email sudah ada
    $check_query = "SELECT id_akun FROM user WHERE username = ? OR email = ?";
    $check_stmt = $conn->prepare($check_query);
    $check_stmt->bind_param("ss", $username, $email);
    $check_stmt->execute();
    $result = $check_stmt->get_result();
    
    if ($result->num_rows > 0) {
        echo json_encode([
            'status' => 'error',
            'message' => 'Username atau email sudah terdaftar'
        ]);
        exit;
    }
    
    // Generate ID akun dengan format yang diminta
    // Format: 1 digit jabatan + 6 digit tanggal (ddmmyy) + 3 digit urutan
    // $jabatan sudah diambil dari input (1=admin, 2=karyawan, 3=pengguna)
    $tanggal = date('dmy'); // Format ddmmyy
    
    // Cari urutan nomor terakhir untuk hari ini
    $today_prefix = $jabatan . $tanggal;
    $urutan_query = "SELECT id_akun FROM user WHERE id_akun LIKE ? ORDER BY id_akun DESC LIMIT 1";
    $urutan_stmt = $conn->prepare($urutan_query);
    $search_pattern = $today_prefix . '%';
    $urutan_stmt->bind_param("s", $search_pattern);
    $urutan_stmt->execute();
    $urutan_result = $urutan_stmt->get_result();
    
    if ($urutan_result->num_rows > 0) {
        $last_id = $urutan_result->fetch_assoc()['id_akun'];
        $last_urutan = intval(substr($last_id, -3));
        $new_urutan = $last_urutan + 1;
    } else {
        $new_urutan = 1;
    }
    
    $urutan_str = str_pad($new_urutan, 3, '0', STR_PAD_LEFT);
    $id_akun = $today_prefix . $urutan_str;
    
    // Generate urutan untuk tabel
    $urutan_query = "SELECT MAX(urutan) as max_urutan FROM user";
    $urutan_result = $conn->query($urutan_query);
    $max_urutan = $urutan_result->fetch_assoc()['max_urutan'];
    $urutan = $max_urutan ? $max_urutan + 1 : 1;
    
    // Hash password
    $hashed_password = password_hash($password, PASSWORD_DEFAULT);
    
    // Insert ke database
    $insert_query = "INSERT INTO user (urutan, id_akun, username, email, password, jabatan, dibuat_pada) VALUES (?, ?, ?, ?, ?, ?, NOW())";
    $insert_stmt = $conn->prepare($insert_query);
    $insert_stmt->bind_param("issssi", $urutan, $id_akun, $username, $email, $hashed_password, $jabatan);
    
    if ($insert_stmt->execute()) {
        echo json_encode([
            'status' => 'success',
            'message' => 'Pendaftaran berhasil',
            'data' => [
                'id_akun' => $id_akun,
                'username' => $username,
                'email' => $email,
                'jabatan' => $jabatan
            ]
        ]);
    } else {
        echo json_encode([
            'status' => 'error',
            'message' => 'Gagal menyimpan data: ' . $conn->error
        ]);
    }
    
    $insert_stmt->close();
    $check_stmt->close();
    $urutan_stmt->close();
    $conn->close();
    
} else {
    echo json_encode([
        'status' => 'error',
        'message' => 'Method tidak diizinkan'
    ]);
}
?>