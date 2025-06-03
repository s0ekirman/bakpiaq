package bakpiaq.com;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class daftarFragment extends Fragment {

    private TextInputLayout tilUsername, tilEmail, tilPassword;
    private TextInputEditText etUsername, etEmail, etPassword;
    private Button btnDaftar;
    private RequestQueue requestQueue;

    public daftarFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daftar, container, false);

        // Initialize views
        tilUsername = view.findViewById(R.id.tilUsername);
        tilEmail = view.findViewById(R.id.tilEmail);
        tilPassword = view.findViewById(R.id.tilPassword);
        etUsername = view.findViewById(R.id.etUsername);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnDaftar = view.findViewById(R.id.btnDaftar);
        requestQueue = Volley.newRequestQueue(getContext());
//

        // Set click listeners
        btnDaftar.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateInput(username, email, password)) {
                registerUser(username, email, password);
            }
        });

        return view;
    }
    private void registerUser(String username, String email, String password) {
        // Disable button untuk mencegah multiple click
        btnDaftar.setEnabled(false);
        btnDaftar.setText("Mendaftar...");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, sambungan.REGISTER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            String message = jsonResponse.getString("message");

                            if (status.equals("success")) {
                                // Registrasi berhasil
                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();

                                // Optional: Get data dari response
                                if (jsonResponse.has("data")) {
                                    JSONObject data = jsonResponse.getJSONObject("data");
                                    String idAkun = data.getString("id_akun");
                                    String regUsername = data.getString("username");
                                    String regEmail = data.getString("email");
                                    int jabatan = data.getInt("jabatan");

                                    // Bisa simpan ke SharedPreferences atau lakukan navigasi
                                    // Contoh: pindah ke fragment login atau main activity
                                    clearForm();
                                }

                            } else {
                                // Registrasi gagal
                                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                        }

                        // Enable kembali button
                        btnDaftar.setEnabled(true);
                        btnDaftar.setText("Daftar");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMessage = "Terjadi kesalahan jaringan";

                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            errorMessage = "Error " + statusCode + ": " + error.getMessage();
                        } else if (error.getMessage() != null) {
                            errorMessage = error.getMessage();
                        }

                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();

                        // Enable kembali button
                        btnDaftar.setEnabled(true);
                        btnDaftar.setText("Daftar");
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("email", email);
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

        // Set timeout (optional)
        // stringRequest.setRetryPolicy(new DefaultRetryPolicy(
        //     30000, // 30 seconds timeout
        //     DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
        //     DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add request to queue
        requestQueue.add(stringRequest);
    }

    // Method untuk clear form setelah registrasi berhasil
    private void clearForm() {
        etUsername.setText("");
        etEmail.setText("");
        etPassword.setText("");
        tilUsername.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
    }

    // Jangan lupa cleanup RequestQueue di onDestroy
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
    }

    private boolean validateInput(String username, String email, String password) {
        boolean isValid = true;

        if (username.isEmpty()) {
            tilUsername.setError("Username tidak boleh kosong");
            isValid = false;
        } else {
            tilUsername.setError(null);
        }

        if (email.isEmpty()) {
            tilEmail.setError("Email tidak boleh kosong");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Format email tidak valid");
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        if (password.isEmpty()) {
            tilPassword.setError("Password tidak boleh kosong");
            isValid = false;
        } else if (password.length() < 8) {
            tilPassword.setError("Password minimal 8 karakter");
            isValid = false;
        } else {
            tilPassword.setError(null);
        }

        return isValid;
    }
}