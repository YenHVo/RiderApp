package edu.uga.cs.riderapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

import edu.uga.cs.riderapp.R;
import edu.uga.cs.riderapp.fragments.CreateProposalFragment;
import edu.uga.cs.riderapp.fragments.ProposalListFragment;
import edu.uga.cs.riderapp.models.User;

public class HomeActivity extends AppCompatActivity {

    private TextView userNameTextView;
    private TextView pointsTextView;
    private Button logoutButton;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userNameTextView = findViewById(R.id.userNameTextView);
        pointsTextView = findViewById(R.id.pointsTextView);
        logoutButton = findViewById(R.id.logoutButton);

        // todo: find a way to get the current user. maybe from the intent?
        //user = getCurrentUser();
        //updateUserInfo();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new ProposalListFragment())
                    .commit();
        }

        // Set click listeners for bottom navigation buttons
        findViewById(R.id.createRideBtn).setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new CreateProposalFragment())
                    .commit();
        });

        findViewById(R.id.homeBtn).setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new ProposalListFragment())
                    .commit();
        });

        // todo: profile fragment?
        /*
        findViewById(R.id.profileBtn).setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new PlacesFragment())
                    .commit();
        });*/

        // Logout button click handler
        logoutButton.setOnClickListener(v -> {
            // todo: make sure to clear out user data somehow
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private User getCurrentUser() {
        if (getIntent().hasExtra("user")) {
            return (User) getIntent().getSerializableExtra("user");
        } else {
            throw new NullPointerException();
        }
    }

    private void updateUserInfo() {
        if (user != null) {
            userNameTextView.setText("Welcome, " + user.getName() + "!");
            pointsTextView.setText(user.getPoints() + " points");
        }
    }

    // todo: uncomment for firebase
    /*
    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }*/
}
