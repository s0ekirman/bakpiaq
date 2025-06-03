import { initializeApp } from "https://www.gstatic.com/firebasejs/11.8.1/firebase-app.js";
import { getAuth, signInWithPopup, GoogleAuthProvider } from "https://www.gstatic.com/firebasejs/11.8.1/firebase-auth.js";

// Firebase configuration
const firebaseConfig = {
    apiKey: "AIzaSyBdXhyQX4ay_SAr4kccdhkFrBIyM-xPWn4",
    authDomain: "bakpiaq-ce0d0.firebaseapp.com",
    projectId: "bakpiaq-ce0d0",
    storageBucket: "bakpiaq-ce0d0.firebasestorage.app",
    messagingSenderId: "194724746659",
    appId: "1:194724746659:web:a8e6d4ca3c378523103616",
    measurementId: "G-W80GK7XWNG"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const provider = new GoogleAuthProvider();

// Function untuk login dengan Google
export async function loginWithGoogle() {
    try {
        const result = await signInWithPopup(auth, provider);
        const user = result.user;
        
        // Data user yang didapat dari Google
        const userData = {
            uid: user.uid,
            email: user.email,
            displayName: user.displayName,
            photoURL: user.photoURL,
            emailVerified: user.emailVerified
        };
        
        // Kirim data ke server PHP untuk disimpan ke database
        const response = await fetch('login_gugel.php', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(userData)
        });
        
        const data = await response.json();
        
        if (data.status === 'success') {
            // Tampilkan pesan sukses
            const messageDiv = document.getElementById('message');
            messageDiv.innerHTML = '<p style="color: green;">' + data.message + '</p>';
            messageDiv.innerHTML += '<p>Selamat datang, ' + data.data.username + '!</p>';
            messageDiv.innerHTML += '<p>Jabatan: ' + data.data.jabatan_nama + '</p>';
            
            // Redirect berdasarkan jabatan
            setTimeout(() => {
                switch(data.data.jabatan) {
                    case 1:
                        window.location.href = 'atmin/index.php';
                        break;
                    case 2:
                        window.location.href = 'staff/index.php';
                        break;
                    case 3:
                        window.location.href = 'user/index.php';
                        break;
                    default:
                        window.location.href = 'user/index.php';
                }
            }, 2000);
        } else {
            throw new Error(data.message);
        }
        
        return data;
        
    } catch (error) {
        console.error('Error during Google login:', error);
        const messageDiv = document.getElementById('message');
        messageDiv.innerHTML = '<p style="color: red;">Login Google gagal: ' + error.message + '</p>';
        throw error;
    }
}

// Event listener untuk tombol login Google
document.addEventListener('DOMContentLoaded', function() {
    // Cari tombol login Google berdasarkan ID
    const googleLoginBtn = document.getElementById('googleLoginBtn');
    if (googleLoginBtn) {
        googleLoginBtn.addEventListener('click', function(e) {
            e.preventDefault();
            console.log('Google login button clicked'); // Debug
            loginWithGoogle();
        });
    } else {
        console.error('Google login button not found');
    }
});