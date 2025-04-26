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

public class MainActivity extends AppCompatActivity {

    Button signUpButton;
    Button loginButton;

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

        signUpButton = findViewById(R.id.sign_up_button);
        loginButton = findViewById(R.id.login_button);

        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AuthActivity.class);
            intent.putExtra("auth_mode", "signup");
            startActivity(intent);
            finish();
        });


        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AuthActivity.class);
            intent.putExtra("auth_mode", "login");
            startActivity(intent);
            finish();
        });
        // todo: delete this, used it for testing
       // loginButton.setOnClickListener(v -> {
         //   Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            //startActivity(intent);
          //  finish();
     //   });

    }
}