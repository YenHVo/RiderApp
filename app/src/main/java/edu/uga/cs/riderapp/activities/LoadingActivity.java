package edu.uga.cs.riderapp.activities;



import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import edu.uga.cs.riderapp.activities.HomeActivity;


import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import edu.uga.cs.riderapp.R;
import edu.uga.cs.riderapp.models.Proposal;
import edu.uga.cs.riderapp.models.Ride;
import edu.uga.cs.riderapp.models.RideHistory;
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

    private Button updateProposalButton, deleteProposalButton;
    private LinearLayout manageProposalButtons;


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
        manageProposalButtons = findViewById(R.id.manageProposalButtons);
        updateProposalButton = findViewById(R.id.updateProposalButton);
        deleteProposalButton = findViewById(R.id.deleteProposalButton);

    }

    private void setupButtonListeners() {
        backButton.setOnClickListener(v -> cancelProposal());
        returnHomeButton.setOnClickListener(v -> navigateToHome());
        completeButton.setOnClickListener(v -> markRideCompleted());
        driverAcceptButton.setOnClickListener(v -> acceptProposal());
        driverRejectButton.setOnClickListener(v -> rejectProposal());
        riderAcceptButton.setOnClickListener(v -> acceptProposal());
        riderRejectButton.setOnClickListener(v -> rejectProposal());
        updateProposalButton.setOnClickListener(v -> updateProposal());
        deleteProposalButton.setOnClickListener(v -> deleteProposal());

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

        manageProposalButtons.setVisibility(View.VISIBLE);
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
        manageProposalButtons.setVisibility(View.GONE);
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
            manageProposalButtons.setVisibility(View.GONE);
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
        manageProposalButtons.setVisibility(View.GONE);

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
        driverButtonContainer.setVisibility(View.GONE);
        riderButtonContainer.setVisibility(View.GONE);
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
        manageProposalButtons.setVisibility(View.GONE);

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

    private void updateProposal() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Update Proposal");

        // Create layout with text fields
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        layout.setPadding(padding, padding, padding, padding);

        // Input fields
        final TextView startLabel = new TextView(this);
        startLabel.setText("Start Location:");
        final android.widget.EditText startInput = new android.widget.EditText(this);

        final TextView endLabel = new TextView(this);
        endLabel.setText("Destination:");
        final android.widget.EditText endInput = new android.widget.EditText(this);

        final TextView dateLabel = new TextView(this);
        dateLabel.setText("Date (e.g., 2025-05-01):");
        final android.widget.EditText dateInput = new android.widget.EditText(this);

        final TextView timeLabel = new TextView(this);
        timeLabel.setText("Time (e.g., 14:30):");
        final android.widget.EditText timeInput = new android.widget.EditText(this);

        layout.addView(startLabel);
        layout.addView(startInput);
        layout.addView(endLabel);
        layout.addView(endInput);
        layout.addView(dateLabel);
        layout.addView(dateInput);
        layout.addView(timeLabel);
        layout.addView(timeInput);

        builder.setView(layout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newStart = startInput.getText().toString().trim();
            String newEnd = endInput.getText().toString().trim();
            String newDate = dateInput.getText().toString().trim();
            String newTime = timeInput.getText().toString().trim();

            if (newStart.isEmpty() || newEnd.isEmpty() || newDate.isEmpty() || newTime.isEmpty()) {
                Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> updates = new HashMap<>();
            updates.put("startLocation", newStart);
            updates.put("endLocation", newEnd);
            updates.put("date", newDate);
            updates.put("time", newTime);

            proposalRef.updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Proposal updated!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update proposal", Toast.LENGTH_SHORT).show();
                    });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void deleteProposal() {
        if (proposalRef != null) {
            proposalRef.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(LoadingActivity.this, "Proposal deleted successfully", Toast.LENGTH_SHORT).show();
                        navigateToHome();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(LoadingActivity.this, "Failed to delete proposal", Toast.LENGTH_SHORT).show();
                    });
        }
    }


    private void acceptProposal() {
        String statusField = isDriver ? "driverStatus" : "riderStatus";

        // Update the status in Firebase
        proposalRef.child(statusField).setValue("accepted")
                .addOnSuccessListener(aVoid -> {
                    // After accepting, check if both have accepted
                    proposalRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            String driverStatus = snapshot.child("driverStatus").getValue(String.class);
                            String riderStatus = snapshot.child("riderStatus").getValue(String.class);
                            if ("accepted".equals(driverStatus) && "accepted".equals(riderStatus)) {
                                // Both the driver and the rider have accepted, show the accepted screen
                                showAcceptedScreen(snapshot.getValue(Proposal.class));

                                DatabaseReference offeredRidesRef = FirebaseDatabase.getInstance().getReference("offered_rides");
                                DatabaseReference requestedRidesRef = FirebaseDatabase.getInstance().getReference("requested_rides");

                                // Handle based on the user's role (driver or rider)
                                if (isDriver) {
                                    requestedRidesRef.child(proposalRef.getKey()).removeValue();
                                } else {
                                    offeredRidesRef.child(proposalRef.getKey()).removeValue();
                                }

                                // Add the ride to accepted rides for both rider and driver
                                if (isDriver) {
                                    acceptRideRequestAsDriver(snapshot); // Driver's side logic
                                } else {
                                    acceptRideRequestAsRider(snapshot); // Rider's side logic
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Handle potential errors
                        }
                    });
                });
    }


    private void acceptRideRequestAsRider(DataSnapshot snapshot) {
        String riderEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String proposalId = proposalRef.getKey();
        String riderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Check for driverId in the proposal (because it's a request)
        proposalRef.child("driverId").get().addOnSuccessListener(driverSnapshot -> {
            String driverId = driverSnapshot.getValue(String.class);
            if (driverId == null) {
                Log.e("acceptRideRequestAsRider", "Missing driverId for request proposal.");
                Toast.makeText(this, "This offer is invalid (missing driver).", Toast.LENGTH_SHORT).show();
                return;
            }

            usersRef.child(driverId).child("email").get().addOnSuccessListener(driverEmailSnapshot -> {
                String driverEmail = driverEmailSnapshot.getValue(String.class);
                if (driverEmail == null) driverEmail = "Unknown";

                proposalRef.child("status").setValue("accepted");

                DatabaseReference acceptedRidesRef = FirebaseDatabase.getInstance().getReference("accepted_rides");

                Ride acceptedRide = new Ride();
                acceptedRide.setProposalId(proposalId);
                acceptedRide.setRiderId(riderId);
                acceptedRide.setDriverEmail(driverEmail);
                acceptedRide.setRiderEmail(riderEmail);

                proposalRef.child("points").get().addOnSuccessListener(pointsSnapshot -> {
                    Long points = pointsSnapshot.getValue(Long.class);
                    acceptedRide.setPoints(points != null ? points : 0);

                    proposalRef.child("dateTime").get().addOnSuccessListener(dateSnapshot -> {
                        Long dateTime = dateSnapshot.getValue(Long.class);
                        acceptedRide.setDateTime(dateTime != null ? dateTime : System.currentTimeMillis());

                        // Save to accepted_rides
                        acceptedRidesRef.child(driverId).child(proposalId).setValue(acceptedRide);
                        acceptedRidesRef.child(riderId).child(proposalId).setValue(acceptedRide);

                        // Update points for rider and driver
                        updatePointsAfterRide(driverId, riderId);

                        Toast.makeText(this, "Ride request accepted successfully", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Log.e("TAG", "Failed to fetch dateTime", e);
                    });

                }).addOnFailureListener(e -> {
                    Log.e("TAG", "Failed to fetch points", e);
                });

            }).addOnFailureListener(e -> {
                Log.e("TAG", "Failed to fetch driver email", e);
            });

        }).addOnFailureListener(e -> {
            Log.e("TAG", "Failed to fetch driverId", e);
        });
    }



    private void acceptRideRequestAsDriver(DataSnapshot snapshot) {
        String driverEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String proposalId = proposalRef.getKey();
        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Check for riderId in the proposal (because it's an offer)
        proposalRef.child("riderId").get().addOnSuccessListener(riderSnapshot -> {
            String riderId = riderSnapshot.getValue(String.class);
            if (riderId == null) {
                Log.e("acceptRideRequestAsDriver", "Missing riderId for offer proposal.");
                Toast.makeText(this, "This request is invalid (missing rider).", Toast.LENGTH_SHORT).show();
                return;
            }

            usersRef.child(riderId).child("email").get().addOnSuccessListener(riderEmailSnapshot -> {
                String riderEmail = riderEmailSnapshot.getValue(String.class);
                if (riderEmail == null) riderEmail = "Unknown";

                proposalRef.child("status").setValue("accepted");

                DatabaseReference acceptedRidesRef = FirebaseDatabase.getInstance().getReference("accepted_rides");

                Ride acceptedRide = new Ride();
                acceptedRide.setProposalId(proposalId);
                acceptedRide.setRiderId(riderId);
                acceptedRide.setDriverEmail(driverEmail);
                acceptedRide.setRiderEmail(riderEmail);

                proposalRef.child("points").get().addOnSuccessListener(pointsSnapshot -> {
                    Long points = pointsSnapshot.getValue(Long.class);
                    acceptedRide.setPoints(points != null ? points : 0);

                    proposalRef.child("dateTime").get().addOnSuccessListener(dateSnapshot -> {
                        Long dateTime = dateSnapshot.getValue(Long.class);
                        acceptedRide.setDateTime(dateTime != null ? dateTime : System.currentTimeMillis());

                        // Save to accepted_rides
                        acceptedRidesRef.child(driverId).child(proposalId).setValue(acceptedRide);
                        acceptedRidesRef.child(riderId).child(proposalId).setValue(acceptedRide);

                        // Update points for rider and driver
                        updatePointsAfterRide(driverId, riderId);

                        Toast.makeText(this, "Ride request accepted successfully", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Log.e("TAG", "Failed to fetch dateTime", e);
                    });

                }).addOnFailureListener(e -> {
                    Log.e("TAG", "Failed to fetch points", e);
                });

            }).addOnFailureListener(e -> {
                Log.e("TAG", "Failed to fetch rider email", e);
            });

        }).addOnFailureListener(e -> {
            Log.e("TAG", "Failed to fetch riderId", e);
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

        // Set confirmation based on user role
        String completedField = isDriver ? "confirmedByDriver" : "confirmedByRider";

        // First, set our confirmation
        proposalRef.child(completedField).setValue(true)
                .addOnSuccessListener(aVoid -> {
                    // Immediately listen for changes in both confirmedByDriver and confirmedByRider fields
                    proposalRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            // Log to verify values
                            Boolean confirmedByDriver = snapshot.child("confirmedByDriver").getValue(Boolean.class);
                            Boolean confirmedByRider = snapshot.child("confirmedByRider").getValue(Boolean.class);

                            Log.d("markRideCompleted", "Driver confirmed: " + confirmedByDriver);
                            Log.d("markRideCompleted", "Rider confirmed: " + confirmedByRider);

                            String driverId = snapshot.child("driverId").getValue(String.class);
                            String riderId = snapshot.child("riderId").getValue(String.class);

                            String startLocation = snapshot.child("startLocation").getValue(String.class);
                            String endLocation = snapshot.child("endLocation").getValue(String.class);
                            Long dateTime = snapshot.child("dateTime").getValue(Long.class);

                            if (startLocation == null || endLocation == null || dateTime == null) {
                                Log.e("markRideCompleted", "Missing fields: startLocation, endLocation, or dateTime");
                                return;
                            }

                            // Proceed only if both are confirmed
                            if (Boolean.TRUE.equals(confirmedByDriver) && Boolean.TRUE.equals(confirmedByRider)) {
                                // Update status to "completed"
                                proposalRef.child("status").setValue("completed");

                                updatePointsAfterRide(driverId, riderId);
                                removeRideFromAcceptedRides(driverId, riderId);
                                saveRideToHistory(driverId, riderId, startLocation, endLocation, dateTime);

                                // Ensure this runs on the main thread
                                runOnUiThread(() -> {
                                    showCompletedScreen();  // This should now work for both users
                                });
                            } else {
                                Toast.makeText(LoadingActivity.this, "Waiting for other party to confirm...", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Toast.makeText(LoadingActivity.this, "Failed to check ride status", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoadingActivity.this, "Failed to confirm ride", Toast.LENGTH_SHORT).show();
                });
    }


                /*
                if (Boolean.TRUE.equals(confirmedByDriver) && Boolean.TRUE.equals(confirmedByRider)) {

                    proposalRef.child("status").setValue("completed");

                    String driverId = snapshot.child("driverId").getValue(String.class);
                    String riderId = snapshot.child("riderId").getValue(String.class);


                    updatePointsAfterRide(driverId, riderId);

                    // Start the HomeActivity and pass the driverId and riderId
                    Intent intent = new Intent(LoadingActivity.this, HomeActivity.class);
                    intent.putExtra("driverId", driverId);
                    intent.putExtra("riderId", riderId);
                    startActivity(intent);

                    Toast.makeText(LoadingActivity.this, "Ride completed!", Toast.LENGTH_SHORT).show();
                    showCompletedScreen();
                } else {
                    Toast.makeText(LoadingActivity.this, "Waiting for other party to confirm...", Toast.LENGTH_SHORT).show();
                }*/


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

    /*
    public void updatePointsAfterRide(String driverId, String riderId) {
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference("users").child(driverId).child("points");
        driverRef.get().addOnSuccessListener(driverSnapshot -> {
            if (driverSnapshot.exists()) {
                Long driverPoints = driverSnapshot.getValue(Long.class);
                if (driverPoints == null) driverPoints = 0L;
                driverRef.setValue(driverPoints + 100).addOnSuccessListener(aVoid -> {
                    Log.d("HomeActivity", "Driver points updated successfully");
                }).addOnFailureListener(e -> {
                    Log.e("HomeActivity", "Failed to update driver points: " + e.getMessage());
                });
            } else {
                Log.e("HomeActivity", "Driver points not found");
            }
        }).addOnFailureListener(e -> Log.e("HomeActivity", "Failed to fetch driver points: " + e.getMessage()));

        DatabaseReference riderRef = FirebaseDatabase.getInstance().getReference("users").child(riderId).child("points");
        riderRef.get().addOnSuccessListener(riderSnapshot -> {
            if (riderSnapshot.exists()) {
                Long riderPoints = riderSnapshot.getValue(Long.class);
                if (riderPoints == null) riderPoints = 0L;
                long updatedPoints = riderPoints - 100;
                if (updatedPoints < 0) updatedPoints = 0;
                riderRef.setValue(updatedPoints).addOnSuccessListener(aVoid -> {
                    Log.d("HomeActivity", "Rider points updated successfully");
                }).addOnFailureListener(e -> {
                    Log.e("HomeActivity", "Failed to update rider points: " + e.getMessage());
                });
            } else {
                Log.e("HomeActivity", "Rider points not found");
            }
        }).addOnFailureListener(e -> Log.e("HomeActivity", "Failed to fetch rider points: " + e.getMessage()));
    }*/

    /*
    public void updatePointsAfterRide(String driverId, String riderId) {
        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference("users").child(driverId).child("points");
        DatabaseReference riderRef = FirebaseDatabase.getInstance().getReference("users").child(riderId).child("points");

        // Update driver points
        driverRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Long points = mutableData.getValue(Long.class);
                if (points == null) {
                    mutableData.setValue(100L);
                } else {
                    mutableData.setValue(points + 100);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
                    Log.d("LoadingActivity", "Driver points updated successfully");
                } else {
                    Log.e("LoadingActivity", "Failed to update driver points: " + databaseError.getMessage());
                }
            }
        });

        // Update rider points
        riderRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Long points = mutableData.getValue(Long.class);
                if (points == null) {
                    mutableData.setValue(0L);
                } else {
                    long newPoints = points - 100;
                    mutableData.setValue(Math.max(newPoints, 0));
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean committed, DataSnapshot dataSnapshot) {
                if (committed) {
                    Log.d("LoadingActivity", "Rider points updated successfully");
                } else {
                    Log.e("LoadingActivity", "Failed to update rider points: " + databaseError.getMessage());
                }
            }
        });

        // Navigate to home after updates
        navigateToHome();
    }*/
    private void saveRideToHistory(String driverId, String riderId, String startLocation, String endLocation, long dateTime) {
        // Create a new RideHistory object
        RideHistory rideHistory = new RideHistory();
        rideHistory.setDriverId(driverId);
        rideHistory.setRiderId(riderId);
        rideHistory.setStartLocation(startLocation);
        rideHistory.setEndLocation(endLocation);
        rideHistory.setDateTime(dateTime);
        rideHistory.setStatus("completed");  // Set status to completed

        // Save the ride history to Firebase for both the driver and rider
        DatabaseReference rideHistoryRef = FirebaseDatabase.getInstance().getReference("ride_history");
        String rideHistoryId = rideHistoryRef.push().getKey();
        if (rideHistoryId != null) {
            rideHistoryRef.child(driverId).child(rideHistoryId).setValue(rideHistory);
            rideHistoryRef.child(riderId).child(rideHistoryId).setValue(rideHistory);
        }
    }

    /*
    private void updatePointsAfterRide(String driverId, String riderId) {
        // Validate IDs first
        if (driverId == null || riderId == null) {
            Log.e("LoadingActivity", "Invalid user IDs - driver: " + driverId + ", rider: " + riderId);
            navigateToHome();
            return;
        }

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference driverRef = usersRef.child(driverId);
        DatabaseReference riderRef = usersRef.child(riderId);

        // Use Task API to fetch both driver and rider data in parallel
        Task<DataSnapshot> driverTask = driverRef.get();
        Task<DataSnapshot> riderTask = riderRef.get();

        Tasks.whenAllSuccess(driverTask, riderTask).addOnSuccessListener(tasks -> {
            // Ensure both tasks were successful
            DataSnapshot driverSnapshot = (DataSnapshot) tasks.get(0);
            DataSnapshot riderSnapshot = (DataSnapshot) tasks.get(1);

            User driver = driverSnapshot.getValue(User.class);
            User rider = riderSnapshot.getValue(User.class);

            if (driver == null || rider == null) {
                Log.e("LoadingActivity", "Driver or Rider data not found");
                navigateToHome();
                return;
            }

            long newDriverPoints = driver.getPoints() + 100;
            long newRiderPoints = Math.max(rider.getPoints() - 100, 0);

            // Perform atomic update for both users' points
            Map<String, Object> updates = new HashMap<>();
            updates.put(driverId + "/points", newDriverPoints);
            updates.put(riderId + "/points", newRiderPoints);

            // Update points in the database
            usersRef.updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("LoadingActivity", "Points updated successfully");
                        navigateToHome();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("LoadingActivity", "Failed to update points", e);
                        navigateToHome();
                    });
        }).addOnFailureListener(e -> {
            Log.e("LoadingActivity", "Failed to retrieve data", e);
            navigateToHome();
        });
    }*/

    private void updatePointsAfterRide(String driverId, String riderId) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        Task<Void> driverUpdate = usersRef.child(driverId).child("points").get().continueWithTask(task -> {
            Long currentDriverPoints = task.getResult().getValue(Long.class);
            if (currentDriverPoints == null) currentDriverPoints = 0L;
            return usersRef.child(driverId).child("points").setValue(currentDriverPoints + 100);
        });

        Task<Void> riderUpdate = usersRef.child(riderId).child("points").get().continueWithTask(task -> {
            Long currentRiderPoints = task.getResult().getValue(Long.class);
            if (currentRiderPoints == null) currentRiderPoints = 0L;
            return usersRef.child(riderId).child("points").setValue(Math.max(currentRiderPoints - 100, 0));
        });

        Tasks.whenAll(driverUpdate, riderUpdate)
                .addOnSuccessListener(aVoid -> {
                    Log.d("LoadingActivity", "Both driver and rider points updated successfully!");
                    navigateToHome();
                })
                .addOnFailureListener(e -> {
                    Log.e("LoadingActivity", "Failed to update points", e);
                    navigateToHome();
                });
    }

    private void removeRideFromAcceptedRides(String driverId, String riderId) {
        DatabaseReference acceptedRidesRef = FirebaseDatabase.getInstance().getReference("accepted_rides");

        // Remove the ride for both the driver and rider
        acceptedRidesRef.child(driverId).child(proposalRef.getKey()).removeValue();
        acceptedRidesRef.child(riderId).child(proposalRef.getKey()).removeValue();
    }


    // private void navigateToHome() {
    //   Intent intent = new Intent(this, HomeActivity.class);
    // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    //startActivity(intent);
    //finish();
    //}


    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}