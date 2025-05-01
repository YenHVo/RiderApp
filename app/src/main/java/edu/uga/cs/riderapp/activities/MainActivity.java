package edu.uga.cs.riderapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import edu.uga.cs.riderapp.R;

/**
 * MainActivity serves as the entry screen for the app.
 * It provides buttons for users to either sign up or log in.
 * Based on the button clicked, it navigates the user to AuthActivity
 * with the appropriate authentication mode.
 */
public class MainActivity extends AppCompatActivity {

    Button signUpButton;
    Button loginButton;

    /**
     * Called when the activity is first created.
     * Sets up the UI and button click listeners.
     *
     * @param savedInstanceState Previously saved state of the activity (if any).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize buttons
        signUpButton = findViewById(R.id.sign_up_button);
        loginButton = findViewById(R.id.login_button);

        // Launch AuthActivity in sign-up mode when sign-up is clicked
        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AuthActivity.class);
            intent.putExtra("auth_mode", "signup");
            startActivity(intent);
            finish();
        });

        // Launch AuthActivity in login mode when login is clicked
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AuthActivity.class);
            intent.putExtra("auth_mode", "login");
            startActivity(intent);
            finish();
        });
    }
}