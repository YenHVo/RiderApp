package edu.uga.cs.riderapp.activities;



import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import edu.uga.cs.riderapp.activities.HomeActivity;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.uga.cs.riderapp.R;
import edu.uga.cs.riderapp.models.Proposal;
import edu.uga.cs.riderapp.models.User;


public class LoadingActivity extends AppCompatActivity {

    private String proposalId;
    private boolean isDriver;
    private String currentUserId;

    private ProgressBar progressBar;
    private ImageButton backButton;
    private TextView loadingText, subText;
    private LinearLayout driverAcceptedLayout, riderAcceptedLayout;
    private TextView driverMatchDetails, riderMatchDetails;
    private LinearLayout driverButtonContainer, riderButtonContainer;
    private Button driverAcceptButton, driverRejectButton;
    private Button riderAcceptButton, riderRejectButton;
    private Button returnHomeButton;
    private LinearLayout dualButtonContainer;
    private Button cancelButton, completeButton;
    private DatabaseReference proposalRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_loading);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        proposalId = getIntent().getStringExtra("proposalId");
        isDriver = getIntent().getBooleanExtra("isDriver", false);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (proposalId == null || currentUserId == null) {
            finish();
            return;
        }

        initializeViews();
        setupButtonListeners();
        setupDatabaseListener();
    }

    private void initializeViews() {
        progressBar = findViewById(R.id.progressBar);
        loadingText = findViewById(R.id.loadingText);
        backButton = findViewById(R.id.backButton);
        subText = findViewById(R.id.subText);
        driverAcceptedLayout = findViewById(R.id.driverAcceptedLayout);
        riderAcceptedLayout = findViewById(R.id.riderAcceptedLayout);
        driverMatchDetails = findViewById(R.id.driverMatchDetails);
        riderMatchDetails = findViewById(R.id.riderMatchDetails);
        returnHomeButton = findViewById(R.id.returnHomeButton);
        dualButtonContainer = findViewById(R.id.dualButtonContainer);
        cancelButton = findViewById(R.id.cancelButton);
        completeButton = findViewById(R.id.completeButton);
        driverButtonContainer = findViewById(R.id.driverButtonContainer);
        riderButtonContainer = findViewById(R.id.riderButtonContainer);
        driverAcceptButton = findViewById(R.id.driverAcceptButton);
        driverRejectButton = findViewById(R.id.driverRejectButton);
        riderAcceptButton = findViewById(R.id.riderAcceptButton);
        riderRejectButton = findViewById(R.id.riderRejectButton);
    }

    private void setupButtonListeners() {
        backButton.setOnClickListener(v -> cancelProposal());
        returnHomeButton.setOnClickListener(v -> navigateToHome());
        completeButton.setOnClickListener(v -> markRideCompleted());
        driverAcceptButton.setOnClickListener(v -> acceptProposal());
        driverRejectButton.setOnClickListener(v -> rejectProposal());
        riderAcceptButton.setOnClickListener(v -> acceptProposal());
        riderRejectButton.setOnClickListener(v -> rejectProposal());
    }

    private void setupDatabaseListener() {
        proposalRef = FirebaseDatabase.getInstance().getReference("proposals").child(proposalId);

        proposalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Proposal proposal = snapshot.getValue(Proposal.class);
                    if (proposal != null) {
                        String driverStatus = proposal.getDriverStatus();
                        String riderStatus = proposal.getRiderStatus();

                        // 1. Initial state - waiting for match
                        if ("pending".equals(driverStatus) && "pending".equals(riderStatus)) {
                            showInitialWaitingState();
                        }
                        // 2. Other party has accepted - show confirmation UI
                        else if (isOtherPartyAccepted(proposal) && isMyStatusPending(proposal)) {
                            showConfirmationScreen(proposal);
                        }
                        // 3. Waiting for other party to confirm my acceptance
                        else if ("accepted".equals(isDriver ? driverStatus : riderStatus) &&
                                "pending".equals(isDriver ? riderStatus : driverStatus)) {
                            showWaitingForConfirmationState();
                        }
                        // 4. Both have accepted - show ride details
                        else if ("accepted".equals(driverStatus) && "accepted".equals(riderStatus)) {
                            showAcceptedScreen(proposal);
                        }
                        // 5. Ride completed
                        else if (Boolean.TRUE.equals(proposal.getConfirmedByDriver()) && Boolean.TRUE.equals(proposal.getConfirmedByRider())) {
                            proposalRef.child("status").setValue("completed");
                            showCompletedScreen();
                        } else if ("completed".equals(driverStatus) || "completed".equals(riderStatus)) {
                            showCompletedScreen();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(LoadingActivity.this, "Failed to load proposal info", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isOtherPartyAccepted(Proposal proposal) {
        return isDriver ? "accepted".equals(proposal.getRiderStatus()) :
                "accepted".equals(proposal.getDriverStatus());
    }

    private boolean isMyStatusPending(Proposal proposal) {
        return isDriver ? "pending".equals(proposal.getDriverStatus()) :
                "pending".equals(proposal.getRiderStatus());
    }

    private void showInitialWaitingState() {
        progressBar.setVisibility(View.VISIBLE);
        loadingText.setVisibility(View.VISIBLE);
        loadingText.setText("Waiting for a match...");
        subText.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.VISIBLE);

        driverAcceptedLayout.setVisibility(View.GONE);
        riderAcceptedLayout.setVisibility(View.GONE);
        driverButtonContainer.setVisibility(View.GONE);
        riderButtonContainer.setVisibility(View.GONE);
        dualButtonContainer.setVisibility(View.GONE);
        returnHomeButton.setVisibility(View.GONE);
    }

    private void showWaitingForConfirmationState() {
        progressBar.setVisibility(View.VISIBLE);
        loadingText.setVisibility(View.VISIBLE);
        loadingText.setText("Waiting for confirmation...");
        subText.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.VISIBLE);

        driverAcceptedLayout.setVisibility(View.GONE);
        riderAcceptedLayout.setVisibility(View.GONE);
        driverButtonContainer.setVisibility(View.GONE);
        riderButtonContainer.setVisibility(View.GONE);
        dualButtonContainer.setVisibility(View.GONE);
        returnHomeButton.setVisibility(View.GONE);
    }

    private void showConfirmationScreen(Proposal proposal) {
        progressBar.setVisibility(View.GONE);
        loadingText.setVisibility(View.GONE);
        subText.setVisibility(View.GONE);
        backButton.setVisibility(View.GONE);
        returnHomeButton.setVisibility(View.GONE);
        dualButtonContainer.setVisibility(View.GONE);

        String otherUserName = isDriver ? proposal.getRiderName() : proposal.getDriverName();

        String details;
        if (isDriver) {
            // Display rider details
            details = String.format("Rider: %s\nPickup: %s\nDropoff: %s",
                    otherUserName != null ? otherUserName : "Rider",
                    proposal.getStartLocation(),
                    proposal.getEndLocation());

            driverAcceptedLayout.setVisibility(View.VISIBLE);
            driverMatchDetails.setText(details);
            riderAcceptedLayout.setVisibility(View.GONE);
            driverButtonContainer.setVisibility(View.VISIBLE);
            riderButtonContainer.setVisibility(View.GONE);
        } else {
            // Display driver details with car information
            details = String.format("Driver: %s\nCar: %s\nAvailable Seats: %d\nPickup: %s\nDropoff: %s",
                    otherUserName != null ? otherUserName : "Driver",
                    proposal.getCar() != null ? proposal.getCar() : "Car not specified",
                    proposal.getAvailableSeats(),
                    proposal.getStartLocation(),
                    proposal.getEndLocation());

            riderAcceptedLayout.setVisibility(View.VISIBLE);
            riderMatchDetails.setText(details);
            driverAcceptedLayout.setVisibility(View.GONE);
            driverButtonContainer.setVisibility(View.GONE);
            riderButtonContainer.setVisibility(View.VISIBLE);
        }
    }

    private void showAcceptedScreen(Proposal proposal) {
        progressBar.setVisibility(View.GONE);
        loadingText.setVisibility(View.GONE);
        subText.setVisibility(View.GONE);
        backButton.setVisibility(View.GONE);

        String otherUserName = isDriver ? proposal.getRiderName() : proposal.getDriverName();

        String details;
        if (isDriver) {
            // Display rider details
            details = String.format("Rider: %s\nPickup: %s\nDropoff: %s",
                    otherUserName != null ? otherUserName : "Rider",
                    proposal.getStartLocation(),
                    proposal.getEndLocation());

            driverAcceptedLayout.setVisibility(View.VISIBLE);
            driverMatchDetails.setText(details);
            riderAcceptedLayout.setVisibility(View.GONE);
        } else {
            // Display driver details with car information
            details = String.format("Driver: %s\nCar: %s\nAvailable Seats: %d\nPickup: %s\nDropoff: %s",
                    otherUserName != null ? otherUserName : "Driver",
                    proposal.getCar() != null ? proposal.getCar() : "Car not specified",
                    proposal.getAvailableSeats(),
                    proposal.getStartLocation(),
                    proposal.getEndLocation());

            riderAcceptedLayout.setVisibility(View.VISIBLE);
            riderMatchDetails.setText(details);
            driverAcceptedLayout.setVisibility(View.GONE);
        }
        returnHomeButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        dualButtonContainer.setVisibility(View.VISIBLE);
    }

    private void showCompletedScreen() {
        progressBar.setVisibility(View.GONE);
        loadingText.setVisibility(View.VISIBLE);
        loadingText.setText("Ride Completed!");
        subText.setVisibility(View.VISIBLE);

        backButton.setVisibility(View.GONE);
        driverAcceptedLayout.setVisibility(View.GONE);
        riderAcceptedLayout.setVisibility(View.GONE);
        driverButtonContainer.setVisibility(View.GONE);
        riderButtonContainer.setVisibility(View.GONE);

        dualButtonContainer.setVisibility(View.GONE);
        returnHomeButton.setVisibility(View.VISIBLE);

        if (isDriver) {
            subText.setText("Thank you for your service. Your points will be adjusted accordingly.");
        } else {
            subText.setText("Thank you for your request. Your points will be adjusted accordingly.");
        }
    }

    private void fetchUserDetails(String userId, Proposal currentProposal, UserDetailsCallback callback) {
        if (userId == null || currentProposal == null) {
            callback.onDetailsFetched("Details not available");
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    callback.onDetailsFetched("User not found");
                    return;
                }

                User user = snapshot.getValue(User.class);
                if (user == null) {
                    callback.onDetailsFetched("User details error");
                    return;
                }

                String carModel = currentProposal.getCar(); // Get car from proposal first
                if (carModel == null || carModel.isEmpty()) {
                    // Fall back to user's car model if not in proposal
                    carModel = snapshot.child("carModel").exists() ?
                            snapshot.child("carModel").getValue(String.class) : "Not specified";
                }

                String userName = user.getName() != null ? user.getName() : "Unknown";
                String seats = currentProposal.getAvailableSeats() > 0 ?
                        String.valueOf(currentProposal.getAvailableSeats()) : "Not specified";

                String details;
                if (isDriver) {
                    // Rider details
                    details = String.format("Rider: %s\nPickup: %s\nDropoff: %s",
                            userName,
                            currentProposal.getStartLocation() != null ? currentProposal.getStartLocation() : "Not specified",
                            currentProposal.getEndLocation() != null ? currentProposal.getEndLocation() : "Not specified");
                } else {
                    // Driver details - include car info
                    details = String.format("Driver: %s\nCar: %s\nSeats: %s\nPickup: %s\nDropoff: %s",
                            userName,
                            carModel,
                            seats,
                            currentProposal.getStartLocation() != null ? currentProposal.getStartLocation() : "Not specified",
                            currentProposal.getEndLocation() != null ? currentProposal.getEndLocation() : "Not specified");
                }

                callback.onDetailsFetched(details);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onDetailsFetched("Error loading details");
                Log.e("LoadingActivity", "Database error: " + error.getMessage());
            }
        });
    }

    interface UserDetailsCallback {
        void onDetailsFetched(String details);
    }

    private void acceptProposal() {
        String statusField = isDriver ? "driverStatus" : "riderStatus";
        proposalRef.child(statusField).setValue("accepted")
                .addOnSuccessListener(aVoid -> {
                    // After accepting, check if both have accepted
                    proposalRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            String driverStatus = snapshot.child("driverStatus").getValue(String.class);
                            String riderStatus = snapshot.child("riderStatus").getValue(String.class);
                            if ("accepted".equals(driverStatus) && "accepted".equals(riderStatus)) {
                                showAcceptedScreen(snapshot.getValue(Proposal.class));
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError error) {}
                    });
                });
    }

    private void rejectProposal() {
        // Reset both statuses to pending
        proposalRef.child("driverStatus").setValue("pending");
        proposalRef.child("riderStatus").setValue("pending")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Proposal declined", Toast.LENGTH_SHORT).show();
                    navigateToHome();
                });
    }

    private void markRideCompleted() {
        if (proposalRef == null) return;

        String completedField = isDriver ? "confirmedByDriver" : "confirmedByRider";
        proposalRef.child(completedField).setValue(true);

        proposalRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Boolean confirmedByDriver = snapshot.child("confirmedByDriver").getValue(Boolean.class);
                Boolean confirmedByRider = snapshot.child("confirmedByRider").getValue(Boolean.class);

                if (Boolean.TRUE.equals(confirmedByDriver) && Boolean.TRUE.equals(confirmedByRider)) {

                    proposalRef.child("status").setValue("completed");

                    String driverId = snapshot.child("driverId").getValue(String.class);
                    String riderId = snapshot.child("riderId").getValue(String.class);


                    Intent intent = new Intent(LoadingActivity.this, HomeActivity.class);
                    intent.putExtra("driverId", driverId);
                    intent.putExtra("riderId", riderId);
                    startActivity(intent);

                    Toast.makeText(LoadingActivity.this, "Ride completed!", Toast.LENGTH_SHORT).show();
                    showCompletedScreen();
                } else {
                    Toast.makeText(LoadingActivity.this, "Waiting for other party to confirm...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(LoadingActivity.this, "Failed to complete ride", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelProposal() {
        if (proposalRef != null) {
            proposalRef.child("status").setValue("cancelled")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(LoadingActivity.this, "Proposal cancelled", Toast.LENGTH_SHORT).show();
                        navigateToHome();
                    })
                    .addOnFailureListener(e -> Toast.makeText(LoadingActivity.this, "Failed to cancel proposal", Toast.LENGTH_SHORT).show());
        }
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

}