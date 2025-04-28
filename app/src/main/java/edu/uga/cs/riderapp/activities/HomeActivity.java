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
import androidx.fragment.app.FragmentTransaction;

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

public class HomeActivity extends AppCompatActivity {

    private TextView userNameTextView;
    private TextView pointsTextView;
    private Button logoutButton;
    private User user;
    private DatabaseReference userRef;
    private ValueEventListener userListener;

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

        initializeFragment();
        setupButtonListeners();
        setupUserDataListener();
    }

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

    private void setupButtonListeners() {
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

        findViewById(R.id.createRideBtn).setOnClickListener(v -> {
            try {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new CreateProposalFragment())
                        .commit();
            } catch (Exception e) {
                Log.e("HomeActivity", "Fragment transaction failed: " + e.getMessage());
            }
        });

        findViewById(R.id.homeBtn).setOnClickListener(v -> {
            try {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new ProposalListFragment())
                        .commit();
            } catch (Exception e) {
                Log.e("HomeActivity", "Fragment transaction failed: " + e.getMessage());
            }
        });

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



        findViewById(R.id.viewAcceptedRidesBtn).setOnClickListener(v -> {

            AcceptedRidesFragment fragment = new AcceptedRidesFragment();


            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();


            transaction.replace(R.id.fragmentContainer, fragment);


            transaction.addToBackStack(null);


            transaction.commit();
        });
    }


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
                    // Get the User object from the snapshot
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



    private void updateUI(User user) {
        if (user != null) {
            userNameTextView.setText("Welcome, " + user.getName() + "!");
            pointsTextView.setText(user.getPoints() + " points");
        }
    }

    private void updateUserInfo() {
        if (user != null) {
            runOnUiThread(() -> {
                userNameTextView.setText("Welcome, " + user.getName() + "!");
                pointsTextView.setText(user.getPoints() + " points");
                Log.d("HomeActivity", "UI updated - Points: " + user.getPoints());
            });
        } else {
            Log.w("HomeActivity", "User object is null during update");
        }
    }

        /*
        userNameTextView = findViewById(R.id.userNameTextView);
        pointsTextView = findViewById(R.id.pointsTextView);
        logoutButton = findViewById(R.id.logoutButton);

        getCurrentUserFromFirebase();

        if (savedInstanceState == null) {
            try {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new ProposalListFragment())
                        .commit();
            } catch (Exception e) {
                Log.e("HomeActivity", "Fragment transaction failed: " + e.getMessage());
            }
        }

        findViewById(R.id.profileBtn).setOnClickListener(v -> {
            try {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new ProfileFragment())
                        .addToBackStack(null) // allows back button to return
                        .commit();
            } catch (Exception e) {
                Log.e("HomeActivity", "Fragment transaction failed: " + e.getMessage());
            }
        });

        findViewById(R.id.createRideBtn).setOnClickListener(v -> {
            try {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new CreateProposalFragment())
                        .commit();
            } catch (Exception e) {
                Log.e("HomeActivity", "Fragment transaction failed: " + e.getMessage());
            }
        });

        findViewById(R.id.homeBtn).setOnClickListener(v -> {
            try {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainer, new ProposalListFragment())
                        .commit();
            } catch (Exception e) {
                Log.e("HomeActivity", "Fragment transaction failed: " + e.getMessage());
            }
        });

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }*/

    /*
    private void getCurrentUserFromFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        user = snapshot.getValue(User.class);
                        updateUserInfo();
                    } else {
                        Toast.makeText(HomeActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(HomeActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(HomeActivity.this, "No user logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
        }
    }*/

    /*
    private void getCurrentUserFromFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);

            userListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        user = snapshot.getValue(User.class);
                        updateUserInfo();
                    } else {
                        Toast.makeText(HomeActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(HomeActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            };
            userRef.addValueEventListener(userListener);
        } else {
            Toast.makeText(HomeActivity.this, "No user logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
        }
    }*/
    private void getCurrentUserFromFirebase() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);

            // Use addValueEventListener instead of addListenerForSingleValueEvent
            // to get real-time updates
            userListener = userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        user = snapshot.getValue(User.class);
                        if (user != null) {
                            // Force update the UI on the main thread
                            runOnUiThread(() -> updateUserInfo());
                        }
                    } else {
                        Toast.makeText(HomeActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(HomeActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(HomeActivity.this, "No user logged in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
        }
    }

    /*
    private void updateUserInfo() {
        if (user != null) {
            userNameTextView.setText("Welcome, " + user.getName() + "!");
            pointsTextView.setText(user.getPoints() + " points");
        } else {
            Toast.makeText(HomeActivity.this, "User info not available", Toast.LENGTH_SHORT).show();
        }
    }*/

    /*
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userRef != null && userListener != null) {
            userRef.removeEventListener(userListener);
        }
    }*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userRef != null && userListener != null) {
            userRef.removeEventListener(userListener);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        /*else {

            Intent intent = getIntent();
            long updatedPoints = intent.getLongExtra("updatedPoints", -1);
            if (updatedPoints != -1) {

                if (user != null) {
                    user.setPoints(updatedPoints);
                    updateUserInfo();
                } else {
                    Log.e("HomeActivity", "User object is not initialized.");
                }
            }
        }*/
    }
}


