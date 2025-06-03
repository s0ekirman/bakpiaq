package bakpiaq.com;

import android.os.Bundle;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class perakunan extends AppCompatActivity {

    private ToggleButton toggleLogin, toggleDaftar;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perakunan);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        toggleLogin = findViewById(R.id.toggleLogin);
        toggleDaftar = findViewById(R.id.toggleDaftar);
        fragmentManager = getSupportFragmentManager();

        // Set default fragment (Login)
        replaceFragment(new loginFragment());
        toggleLogin.setChecked(true);

        // Set listeners
        toggleLogin.setOnClickListener(v -> {
            if (!toggleLogin.isChecked()) {
                toggleLogin.setChecked(true);
                return;
            }
            toggleDaftar.setChecked(false);
            replaceFragment(new loginFragment());
        });

        toggleDaftar.setOnClickListener(v -> {
            if (!toggleDaftar.isChecked()) {
                toggleDaftar.setChecked(true);
                return;
            }
            toggleLogin.setChecked(false);
            replaceFragment(new daftarFragment());
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }
}