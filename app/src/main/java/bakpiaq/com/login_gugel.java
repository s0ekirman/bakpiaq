package bakpiaq.com;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class login_gugel {
    private static final String TAG = "GoogleLogin";
    private static final int RC_SIGN_IN = 9001;

    private Context context;
    private GoogleSignInClient googleSignInClient;
    private RequestQueue requestQueue;
    private GoogleLoginCallback callback;

    public interface GoogleLoginCallback {
        void onGoogleLoginSuccess(user userData);
        void onGoogleLoginFailure(String errorMessage);
        void onGoogleSignInStart();
    }

    public login_gugel(Context context, GoogleLoginCallback callback) {
        this.context = context;
        this.callback = callback;
        this.requestQueue = Volley.newRequestQueue(context);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .build();

        googleSignInClient = GoogleSignIn.getClient(context, gso);
    }

    public GoogleSignInClient getGoogleSignInClient() {
        return googleSignInClient;
    }

    public void signOut() {
        googleSignInClient.signOut();
    }

    public void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                sendGoogleDataToServer(account);
            }
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            if (callback != null) {
                callback.onGoogleLoginFailure("Google Sign-In gagal: " + e.getMessage());
            }
        }
    }

    private void sendGoogleDataToServer(GoogleSignInAccount account) {
        if (callback != null) {
            callback.onGoogleSignInStart();
        }

        // Create JSON object with Google account data
        JSONObject googleData = new JSONObject();
        try {
            googleData.put("uid", account.getId());
            googleData.put("email", account.getEmail());
            googleData.put("displayName", account.getDisplayName());
            googleData.put("photoURL", account.getPhotoUrl() != null ? account.getPhotoUrl().toString() : "");
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON data", e);
            if (callback != null) {
                callback.onGoogleLoginFailure("Error preparing data: " + e.getMessage());
            }
            return;
        }

        // Send data to server
        StringRequest stringRequest = new StringRequest(Request.Method.POST, sambungan.GOOGLE_LOGIN_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Server response: " + response);
                        handleServerResponse(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Server request failed", error);

                        String errorMessage = "Koneksi ke server gagal";
                        if (error.networkResponse != null) {
                            errorMessage += " (Code: " + error.networkResponse.statusCode + ")";
                        }
                        if (error.getMessage() != null) {
                            errorMessage += ": " + error.getMessage();
                        }

                        if (callback != null) {
                            callback.onGoogleLoginFailure(errorMessage);
                        }
                    }
                }) {

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                return googleData.toString().getBytes();
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void handleServerResponse(String responseBody) {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            String status = jsonResponse.getString("status");
            String message = jsonResponse.getString("message");

            if ("success".equals(status)) {
                JSONObject userData = jsonResponse.getJSONObject("data");
                int jabatan = userData.getInt("jabatan");

                // Cek apakah jabatan adalah pengguna (3)
                if (jabatan == 3) {
                    // Create user object
                    user userObj = new user(
                            userData.getInt("user_id"),
                            userData.getString("id_akun"),
                            userData.getString("username"),
                            userData.getString("email"),
                            jabatan,
                            userData.getString("jabatan_nama"),
                            "", // dibuat_pada tidak diperlukan untuk response
                            "" // redirect_url tidak diperlukan untuk response
                    );

                    if (callback != null) {
                        callback.onGoogleLoginSuccess(userObj);
                    }
                } else {
                    // Jabatan bukan pengguna
                    if (callback != null) {
                        callback.onGoogleLoginFailure("Role anda tidak sesuai. Aplikasi ini hanya untuk pengguna.");
                    }
                }
            } else {
                // Login gagal
                if (callback != null) {
                    callback.onGoogleLoginFailure(message);
                }
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing server response", e);
            if (callback != null) {
                callback.onGoogleLoginFailure("Error parsing response: " + e.getMessage());
            }
        }
    }

    public void cancelRequests() {
        if (requestQueue != null) {
            requestQueue.cancelAll(TAG);
        }
    }
}