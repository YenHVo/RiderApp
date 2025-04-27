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

        //todo: profile fragment?
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
    }

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
    }

    private void updateUserInfo() {
        if (user != null) {
            userNameTextView.setText("Welcome, " + user.getName() + "!");
            pointsTextView.setText(user.getPoints() + " points");
        } else {
            Toast.makeText(HomeActivity.this, "User info not available", Toast.LENGTH_SHORT).show();
        }
    }

    public void updatePointsAfterRide(String driverId, String riderId) {

        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference("users").child(driverId).child("points");
        driverRef.get().addOnSuccessListener(driverSnapshot -> {
            if (driverSnapshot.exists()) {
                Long driverPoints = driverSnapshot.getValue(Long.class);
                if (driverPoints == null) driverPoints = 0L;
                driverRef.setValue(driverPoints + 100);
            }
        }).addOnFailureListener(e -> Log.e("HomeActivity", "Failed to update driver points: " + e.getMessage()));


        DatabaseReference riderRef = FirebaseDatabase.getInstance().getReference("users").child(riderId).child("points");
        riderRef.get().addOnSuccessListener(riderSnapshot -> {
            if (riderSnapshot.exists()) {
                Long riderPoints = riderSnapshot.getValue(Long.class);
                if (riderPoints == null) riderPoints = 0L;
                long updatedPoints = riderPoints - 100;
                if (updatedPoints < 0) updatedPoints = 0;
                riderRef.setValue(updatedPoints);
            }
        }).addOnFailureListener(e -> Log.e("HomeActivity", "Failed to update rider points: " + e.getMessage()));
    }


    @Override
              protected void onStart() {
                  super.onStart();
                  if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                      startActivity(new Intent(this, MainActivity.class));
                      finish();

                      Intent intent = getIntent();
                      String driverId = intent.getStringExtra("driverId");
                      String riderId = intent.getStringExtra("riderId");

                      if (driverId != null && riderId != null) {
                          updatePointsAfterRide(driverId, riderId);
                  }
              }
          }
      }
