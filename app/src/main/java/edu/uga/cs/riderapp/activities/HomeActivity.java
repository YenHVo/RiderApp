package edu.uga.cs.riderapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.uga.cs.riderapp.R;
import edu.uga.cs.riderapp.fragments.AcceptedRidesFragment;
import edu.uga.cs.riderapp.fragments.CreateProposalFragment;
import edu.uga.cs.riderapp.fragments.ProfileFragment;
import edu.uga.cs.riderapp.fragments.ProposalListFragment;
import edu.uga.cs.riderapp.models.User;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * HomeActivity is the main screen for authenticated users.
 * It displays user-specific information such as name and points,
 * and provides navigation to different parts of the app like profile,
 * ride creation, proposal list, and accepted rides.
 */
public class HomeActivity extends AppCompatActivity {

    private TextView userNameTextView;
    private TextView pointsTextView;
    private Button logoutButton;
    private User user;
    private DatabaseReference userRef;
    private ValueEventListener userListener;

    /**
     * Called when the activity is created.
     * Sets up the UI, initializes fragments, sets up button listeners,
     * and attaches a Firebase listener to fetch user data.
     */
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

        // Initialize UI components
        userNameTextView = findViewById(R.id.userNameTextView);
        pointsTextView = findViewById(R.id.pointsTextView);
        logoutButton = findViewById(R.id.logoutButton);

        // Load default fragment and set up listeners
        initializeFragment();
        setupButtonListeners();
        setupUserDataListener();
    }

    /**
     * Loads the default fragment (ProposalListFragment) if no fragment is currently loaded.
     */
    private void initializeFragment() {
        if (getSupportFragmentManager().findFragmentById(R.id.fragmentContainer) == null) {
            try {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new ProposalListFragment())
                        .commit();
            } catch (Exception e) {
                Log.e("HomeActivity", "Fragment transaction failed: " + e.getMessage());
            }
        }
    }

    /**
     * Sets click listeners for the profile, create ride, home, logout, and accepted rides buttons.
     * Each listener performs a fragment transaction or authentication action.
     */
    private void setupButtonListeners() {
        // Open the Profile fragment
        findViewById(R.id.profileBtn).setOnClickListener(v -> {
            try {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new ProfileFragment())
                        .addToBackStack(null)
                        .commit();
            } catch (Exception e) {
                Log.e("HomeActivity", "Fragment transaction failed: " + e.getMessage());
            }
        });

        // Open the CreateProposal fragment
        findViewById(R.id.createRideBtn).setOnClickListener(v -> {
            try {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new CreateProposalFragment())
                        .commit();
            } catch (Exception e) {
                Log.e("HomeActivity", "Fragment transaction failed: " + e.getMessage());
            }
        });

        // Returns to home fragment
        findViewById(R.id.homeBtn).setOnClickListener(v -> {
            try {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new ProposalListFragment())
                        .commit();
            } catch (Exception e) {
                Log.e("HomeActivity", "Fragment transaction failed: " + e.getMessage());
            }
        });

        // Open the AcceptedRides fragment
        findViewById(R.id.viewAcceptedRidesBtn).setOnClickListener(v -> {
            try {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new AcceptedRidesFragment())
                        .commit();
            } catch (Exception e) {
                Log.e("HomeActivity", "Fragment transaction failed: " + e.getMessage());
            }
        });

        // Log out the user
        logoutButton.setOnClickListener(v -> {
            if (userRef != null && userListener != null) {
                userRef.removeEventListener(userListener);
            }

            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Sets up a Firebase listener to fetch and listen for real-time updates
     * to the current user's data.
     * Redirects to MainActivity if user is not logged in.
     */
    private void setupUserDataListener() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Remove any existing listener if it exists
        if (userRef != null && userListener != null) {
            userRef.removeEventListener(userListener);
        }

        // Get the reference to the user's data in Firebase
        userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());

        // Set up the listener for user data
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        // Update the UI with the latest points and other data
                        updateUI(user);
                        Log.d("HomeActivity", "User data updated - Points: " + user.getPoints());
                    }
                } else {
                    Log.w("HomeActivity", "User data does not exist in the database.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("HomeActivity", "Failed to load user data", error.toException());
                Toast.makeText(HomeActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
        };

        // Add the ValueEventListener to fetch data from Firebase
        userRef.addValueEventListener(userListener);
    }

    /**
     * Updates the welcome message and point balance in the UI.
     *
     * @param user The user whose data is displayed.
     */
    private void updateUI(User user) {
        if (user != null) {
            userNameTextView.setText("Welcome, " + user.getName() + "!");
            pointsTextView.setText(user.getPoints() + " points");
        }
    }

    /**
     * Removes the Firebase event listener when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userRef != null && userListener != null) {
            userRef.removeEventListener(userListener);
        }
    }

    /**
     * Called when the activity becomes visible. Checks if the user is still logged in.
     * If not, redirects to the login screen.
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }
}


