<?php
session_start();
header('Content-Type: application/json');

// Include koneksi database yang sudah ada
include 'koneksi.php';

// Ambil data JSON dari request
$input = file_get_contents('php://input');
$userData = json_decode($input, true);

if (!$userData) {
    echo json_encode([
        'status' => 'error',
        'message' => 'Data tidak valid'
    ]);
    exit;
}

try {
    // Cek apakah user sudah ada berdasarkan email
    $stmt = $conn->prepare("SELECT * FROM user WHERE email = ?");
    $stmt->bind_param("s", $userData['email']);
    $stmt->execute();
    $result = $stmt->get_result();
    $existingUser = $result->fetch_assoc();
    
    if ($existingUser) {
        // User sudah ada, update data Google jika belum ada
        if (empty($existingUser['gugel'])) {
            $gugelData = json_encode([
                'uid' => $userData['uid'],
                'photoURL' => $userData['photoURL']
            ]);
            
            $updateStmt = $conn->prepare("UPDATE user SET gugel = ? WHERE email = ?");
            $updateStmt->bind_param("ss", $gugelData, $userData['email']);
            $updateStmt->execute();
            $updateStmt->close();
        }
        
        $user = $existingUser;
    } else {
        // User baru, buat akun baru dengan jabatan default (user = 3)
        // Generate id_akun otomatis
        $maxIdQuery = $conn->query("SELECT MAX(CAST(id_akun AS UNSIGNED)) as max_id FROM user WHERE id_akun REGEXP '^[0-9]+$'");
        $maxIdResult = $maxIdQuery->fetch_assoc();
        $maxId = $maxIdResult['max_id'] ?? 0;
        $newIdAkun = str_pad($maxId + 1, 10, '0', STR_PAD_LEFT);
        
        // Gunakan displayName sebagai username, jika tidak ada gunakan bagian sebelum @ dari email
        $username = $userData['displayName'] ?: explode('@', $userData['email'])[0];
        
        $gugelData = json_encode([
            'uid' => $userData['uid'],
            'photoURL' => $userData['photoURL']
        ]);
        
        $hashedPassword = password_hash('google_login_' . $userData['uid'], PASSWORD_DEFAULT);
        
        $insertStmt = $conn->prepare("INSERT INTO user (id_akun, username, email, password, jabatan, gugel, dibuat_pada) VALUES (?, ?, ?, ?, '3', ?, NOW())");
        $insertStmt->bind_param("sssss", $newIdAkun, $username, $userData['email'], $hashedPassword, $gugelData);
        $insertStmt->execute();
        
        // Ambil data user yang baru dibuat
        $userId = $conn->insert_id;
        $userStmt = $conn->prepare("SELECT * FROM user WHERE urutan = ?");
        $userStmt->bind_param("i", $userId);
        $userStmt->execute();
        $userResult = $userStmt->get_result();
        $user = $userResult->fetch_assoc();
        
        $insertStmt->close();
        $userStmt->close();
    }
    
    $stmt->close();
    
    // Ambil nama jabatan
    $jabatanNames = [
        '1' => 'Admin',
        '2' => 'Staff/Karyawan', 
        '3' => 'User/Pengguna'
    ];
    
    $jabatanNama = $jabatanNames[$user['jabatan']] ?? 'User';
    
    // Decode gugel data untuk mendapatkan photo URL
    $gugelData = json_decode($user['gugel'], true);
    $photoURL = $gugelData['photoURL'] ?? null;
    
    // Set session untuk login Google
    $_SESSION['user_id'] = $user['urutan'];
    $_SESSION['username'] = $user['username'];
    $_SESSION['email'] = $user['email'];
    $_SESSION['jabatan'] = (int)$user['jabatan'];
    $_SESSION['login_method'] = 'google';
    
    // Response sukses
    echo json_encode([
        'status' => 'success',
        'message' => 'Login Google berhasil!',
        'data' => [
            'user_id' => $user['urutan'],
            'id_akun' => $user['id_akun'],
            'username' => $user['username'],
            'email' => $user['email'],
            'jabatan' => (int)$user['jabatan'],
            'jabatan_nama' => $jabatanNama,
            'google_photo' => $photoURL,
            'login_method' => 'google',
            'redirect_url' => getRedirectUrl($user['jabatan'])
        ]
    ]);
    
} catch (Exception $e) {
    echo json_encode([
        'status' => 'error',
        'message' => 'Terjadi kesalahan: ' . $e->getMessage()
    ]);
}

// Tutup koneksi
$conn->close();

function getRedirectUrl($jabatan) {
    switch($jabatan) {
        case '1':
            return 'atmin/index.php';
        case '2':
            return 'staff/index.php';
        case '3':
            return 'user/index.php';
        default:
            return 'user/index.php';
    }
}
?>