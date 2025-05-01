package edu.uga.cs.riderapp.activities;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import edu.uga.cs.riderapp.R;
import edu.uga.cs.riderapp.fragments.LoginFragment;
import edu.uga.cs.riderapp.fragments.SignUpFragment;

/**
 * AuthActivity is an Android activity that handles user authentication.
 * It determines whether to show the login or sign-up screen based on the intent
 * extra "auth_mode". This activity sets up edge-to-edge display and dynamically loads the appropriate
 * fragment into the UI.
 */
public class AuthActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created.
     * Sets up the layout and loads the appropriate authentication fragment.
     *
     * @param savedInstanceState The previously saved state of the activity, or null if none exists.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Determine which fragment to load
        String authMode = getIntent().getStringExtra("auth_mode");
        Fragment fragment;
        if ("signup".equals(authMode)) {
            fragment = new SignUpFragment();
        } else {
            fragment = new LoginFragment();
        }

        // Load the selected fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
