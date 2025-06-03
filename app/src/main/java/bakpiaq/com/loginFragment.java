package bakpiaq.com;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class loginFragment extends Fragment implements login_gugel.GoogleLoginCallback {

    private static final String TAG = "LoginFragment";
    private static final int RC_SIGN_IN = 9001;

    private TextInputLayout tilUsername, tilPassword;
    private TextInputEditText etUsername, etPassword;
    private Button btnLogin, btnGoogleLogin;
    private TextView tvForgotPassword;
    private RequestQueue requestQueue;
    private login_gugel googleLoginHandler;

    public loginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(getContext());

        // Initialize Google Login Handler
        googleLoginHandler = new login_gugel(getContext(), this);

        // Initialize views
        tilUsername = view.findViewById(R.id.tilUsername);
        tilPassword = view.findViewById(R.id.tilPassword);
        etUsername = view.findViewById(R.id.etUsername);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnGoogleLogin = view.findViewById(R.id.btnGoogleLogin);
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);

        // Set click listeners
        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateInput(username, password)) {
                performLogin(username, password);
            }
        });

        btnGoogleLogin.setOnClickListener(v -> {
            // Start Google Sign-In
            signInWithGoogle();
        });

        tvForgotPassword.setOnClickListener(v -> {
            // Handle forgot password here
            Toast.makeText(getContext(), "Lupa Password?", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private boolean validateInput(String username, String password) {
        boolean isValid = true;

        if (username.isEmpty()) {
            tilUsername.setError("Username/Email tidak boleh kosong");
            isValid = false;
        } else {
            tilUsername.setError(null);
        }

        if (password.isEmpty()) {
            tilPassword.setError("Password tidak boleh kosong");
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        return isValid;
    }

    private void performLogin(String username, String password) {
        // Disable button sementara login
        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        // Create StringRequest
        StringRequest stringRequest = new StringRequest(Request.Method.POST, sambungan.LOGIN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Login response: " + response);
                        btnLogin.setEnabled(true);
                        btnLogin.setText("LOGIN");
                        handleLoginResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Login request failed", error);
                        btnLogin.setEnabled(true);
                        btnLogin.setText("LOGIN");

                        String errorMessage = "Koneksi gagal";
                        if (error.networkResponse != null) {
                            errorMessage += " (Code: " + error.networkResponse.statusCode + ")";
                        }
                        if (error.getMessage() != null) {
                            errorMessage += ": " + error.getMessage();
                        }

                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };

        // Add request to queue
        requestQueue.add(stringRequest);
    }

    private void handleLoginResponse(String responseBody) {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            String status = jsonResponse.getString("status");
            String message = jsonResponse.getString("message");

            if ("success".equals(status)) {
                JSONObject userData = jsonResponse.getJSONObject("data");
                int jabatan = userData.getInt("jabatan");
                String jabatanNama = userData.getString("jabatan_nama");
                String username = userData.getString("username");

                // Cek apakah jabatan adalah pengguna (3)
                if (jabatan == 3) {
                    // Login berhasil untuk pengguna
                    Toast.makeText(getContext(), "Login berhasil! Selamat datang " + username, Toast.LENGTH_SHORT).show();

                    // Pindah ke MainActivity
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    // Pass user data ke MainActivity jika diperlukan
                    intent.putExtra("user_id", userData.getString("id_akun"));
                    intent.putExtra("username", username);
                    intent.putExtra("email", userData.getString("email"));
                    intent.putExtra("jabatan", jabatan);
                    intent.putExtra("jabatan_nama", jabatanNama);
                    intent.putExtra("urutan", userData.getInt("urutan"));
                    intent.putExtra("dibuat_pada", userData.getString("dibuat_pada"));

                    startActivity(intent);
                    if (getActivity() != null) {
                        getActivity().finish(); // Close login activity
                    }
                } else {
                    // Jabatan bukan pengguna (admin atau karyawan)
                    Toast.makeText(getContext(), "Role anda tidak sesuai. Aplikasi ini hanya untuk pengguna.", Toast.LENGTH_LONG).show();
                }
            } else {
                // Login gagal
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing login response", e);
            Toast.makeText(getContext(), "Error parsing response: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // Google Sign-In Methods
    private void signInWithGoogle() {
        Intent signInIntent = googleLoginHandler.getGoogleSignInClient().getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            googleLoginHandler.handleSignInResult(GoogleSignIn.getSignedInAccountFromIntent(data));
        }
    }

    // Google Login Callback Methods
    @Override
    public void onGoogleSignInStart() {
        // Disable Google login button while processing
        btnGoogleLogin.setEnabled(false);
        btnGoogleLogin.setText("Signing in...");
    }

    @Override
    public void onGoogleLoginSuccess(user userData) {
        // Re-enable button
        btnGoogleLogin.setEnabled(true);
        btnGoogleLogin.setText("LOGIN WITH GOOGLE");

        // Show success message
        Toast.makeText(getContext(), "Login Google berhasil! Selamat datang " + userData.getUsername(), Toast.LENGTH_SHORT).show();

        // Navigate to MainActivity
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.putExtra("user_id", userData.getId_akun());
        intent.putExtra("username", userData.getUsername());
        intent.putExtra("email", userData.getEmail());
        intent.putExtra("jabatan", userData.getJabatan());
        intent.putExtra("jabatan_nama", userData.getJabatan_nama());
        intent.putExtra("urutan", userData.getUrutan());
        intent.putExtra("login_method", "google");

        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish(); // Close login activity
        }
    }

    @Override
    public void onGoogleLoginFailure(String errorMessage) {
        // Re-enable button
        btnGoogleLogin.setEnabled(true);
        btnGoogleLogin.setText("LOGIN WITH GOOGLE");

        // Show error message
        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
        if (googleLoginHandler != null) {
            googleLoginHandler.cancelRequests();
        }
    }
}