package edu.uga.cs.riderapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
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

        listenToFinalStatus();
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

                        if ("completed".equals(driverStatus) && "completed".equals(riderStatus)) {
                            showCompletedScreen();
                            return;
                        }

                        if ("accepted".equals(driverStatus) && "accepted".equals(riderStatus)) {
                            long currentTimeMillis = System.currentTimeMillis();
                            long rideTimeMillis = proposal.getDateTime();

                            if (rideTimeMillis > currentTimeMillis + (3 * 60 * 1000)) {
                                moveRideToAccepted(snapshot);
                                return;
                            } else {
                                showAcceptedScreen(proposal);
                                return;
                            }
                        }

                        if (isOtherPartyAccepted(proposal) && isMyStatusPending(proposal)) {
                            showConfirmationScreen(proposal);
                            return;
                        }

                        if ("accepted".equals(isDriver ? driverStatus : riderStatus) &&
                                "pending".equals(isDriver ? riderStatus : driverStatus)) {
                            showWaitingForConfirmationState();
                            return;
                        }

                        if ("pending".equals(driverStatus) && "pending".equals(riderStatus)) {
                            showInitialWaitingState();
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
                                // Both the driver and the rider have accepted
                                checkRideDateAndMove(snapshot);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            // Handle potential errors
                        }
                    });
                });
    }

        private void listenToFinalStatus() {
            DatabaseReference proposalRef = FirebaseDatabase.getInstance().getReference()
                    .child("accepted_rides")
                    .child(proposalId);

            proposalRef.child("finalStatus").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String finalStatus = snapshot.getValue(String.class);
                    if (finalStatus == null) return;

                    if ("accepted".equals(finalStatus)) {
                        navigateToHome();
                    } else if ("past".equals(finalStatus)) {
                        navigateToLoadingActivity();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e("listenToFinalStatus", "Failed to listen to finalStatus: " + error.getMessage());
                }
            });
        }


    private void checkRideDateAndMove(DataSnapshot snapshot) {
        Long dateTimeMillis = snapshot.child("dateTime").getValue(Long.class);
        if (dateTimeMillis == null) {
            Log.e("checkRideDateAndMove", "DateTime is missing.");
            return;
        }

        long currentTimeMillis = System.currentTimeMillis();

        if (dateTimeMillis > currentTimeMillis) {
            // Future ride: set finalStatus to "accepted"
            proposalRef.child("finalStatus").setValue("accepted")
                    .addOnSuccessListener(aVoid -> {
                        moveRideToAccepted(snapshot);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("checkRideDateAndMove", "Failed to set finalStatus to accepted.", e);
                    });
        } else {
            // Past or now ride: set finalStatus to "past"
            proposalRef.child("finalStatus").setValue("past")
                    .addOnSuccessListener(aVoid -> {
                        navigateToLoadingActivity();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("checkRideDateAndMove", "Failed to set finalStatus to past.", e);
                    });
        }
    }

    /*
    private void moveRideToAccepted(DataSnapshot snapshot) {
        String proposalId = proposalRef.getKey();
        String driverId = snapshot.child("driverId").getValue(String.class);
        String riderId = snapshot.child("riderId").getValue(String.class);

        if (driverId == null || riderId == null || proposalId == null) {
            Log.e("moveRideToAccepted", "Missing essential data for accepted ride.");
            return;
        }

        DatabaseReference acceptedRidesRef = FirebaseDatabase.getInstance().getReference("accepted_rides");

        // Move the ride to accepted_rides
        Ride acceptedRide = snapshot.getValue(Ride.class);
        acceptedRidesRef.child(driverId).child(proposalId).setValue(acceptedRide);
        acceptedRidesRef.child(riderId).child(proposalId).setValue(acceptedRide);

        // Remove from offered and requested rides
        DatabaseReference offeredRidesRef = FirebaseDatabase.getInstance().getReference("offered_rides");
        DatabaseReference requestedRidesRef = FirebaseDatabase.getInstance().getReference("requested_rides");

        if (isDriver) {
            requestedRidesRef.child(proposalId).removeValue();
        } else {
            offeredRidesRef.child(proposalId).removeValue();
        }

        // Show message and navigate to home
        Toast.makeText(this, "Ride moved to accepted rides.", Toast.LENGTH_SHORT).show();
        navigateToHome();
    }*/

    private void moveRideToAccepted(DataSnapshot snapshot) {
        String proposalId = proposalRef.getKey();
        String driverId = snapshot.child("driverId").getValue(String.class);
        String riderId = snapshot.child("riderId").getValue(String.class);

        if (driverId == null || riderId == null || proposalId == null) {
            Log.e("moveRideToAccepted", "Missing essential data for accepted ride.");
            return;
        }

        DatabaseReference acceptedRidesRef = FirebaseDatabase.getInstance().getReference("accepted_rides");

        Ride acceptedRide = new Ride();
        acceptedRide.setProposalId(proposalId);
        acceptedRide.setDriverId(driverId);
        acceptedRide.setRiderId(riderId);

        acceptedRide.setStartLocation(snapshot.child("startLocation").getValue(String.class));
        acceptedRide.setEndLocation(snapshot.child("endLocation").getValue(String.class));
        acceptedRide.setDateTime(snapshot.child("dateTime").getValue(Long.class));

        acceptedRide.setPoints(100L); // Points for the driver

        Ride acceptedRideForRider = new Ride(acceptedRide);
        acceptedRideForRider.setPoints(-100L); // Points for the rider

        acceptedRidesRef.child(driverId).child(proposalId).setValue(acceptedRide);
        acceptedRidesRef.child(riderId).child(proposalId).setValue(acceptedRideForRider);

        // Remove from offered and requested rides
        DatabaseReference offeredRidesRef = FirebaseDatabase.getInstance().getReference("offered_rides");
        DatabaseReference requestedRidesRef = FirebaseDatabase.getInstance().getReference("requested_rides");

        if (isDriver) {
            requestedRidesRef.child(proposalId).removeValue();
        } else {
            offeredRidesRef.child(proposalId).removeValue();
        }

        Toast.makeText(this, "Ride moved to accepted rides.", Toast.LENGTH_SHORT).show();
        navigateToHome();
    }

    private void navigateToLoadingActivity() {
        Intent intent = new Intent(this, LoadingActivity.class);
        startActivity(intent);
        finish();
    }



    private void setFinalStatusToPast() {
        proposalRef.child("finalStatus").setValue("past").addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                navigateToLoadingActivity();
            }
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

        String statusField = isDriver ? "driverStatus" : "riderStatus";

        proposalRef.child(statusField).setValue("completed")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(LoadingActivity.this, "Marked as completed! Waiting for other user.", Toast.LENGTH_SHORT).show();
                    checkAndSaveIfBothCompleted();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoadingActivity.this, "Failed to mark as completed", Toast.LENGTH_SHORT).show();
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


    private void checkAndSaveIfBothCompleted() {
        proposalRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String driverStatus = snapshot.child("driverStatus").getValue(String.class);
                String riderStatus = snapshot.child("riderStatus").getValue(String.class);

                if ("completed".equals(driverStatus) && "completed".equals(riderStatus)) {
                    saveRideToHistory();
                    removeCompletedRides();
                    String driverId = snapshot.child("driverId").getValue(String.class);
                    String riderId = snapshot.child("riderId").getValue(String.class);
                    if (driverId != null && riderId != null) {
                        updatePointsAfterRide(driverId, riderId);
                    }
                } else {
                    Log.d("checkAndSaveIfBothCompleted", "Waiting for both users to complete.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("checkAndSaveIfBothCompleted", "Failed to check completion status: " + error.getMessage());
            }
        });
    }
    private void saveRideToHistory() {
        if (proposalRef == null) return;

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String rideId = proposalRef.getKey();

        if (rideId == null || currentUserId == null) {
            Log.e("saveRideToHistory", "Missing rideId or currentUserId.");
            return;
        }

        proposalRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String startLocation = snapshot.child("startLocation").getValue(String.class);
                String endLocation = snapshot.child("endLocation").getValue(String.class);
                Long dateTimeMillis = snapshot.child("dateTime").getValue(Long.class);
                String driverId = snapshot.child("driverId").getValue(String.class);
                String riderId = snapshot.child("riderId").getValue(String.class);

                if (startLocation == null || endLocation == null || dateTimeMillis == null || driverId == null || riderId == null) {
                    Log.e("saveRideToHistory", "Missing ride details.");
                    return;
                }

                String driverRole = "Driver";
                String riderRole = "Rider";

                // Build minimal ride history map
                Map<String, Object> driverHistory = new HashMap<>();
                driverHistory.put("role", driverRole);
                driverHistory.put("startLocation", startLocation);
                driverHistory.put("endLocation", endLocation);
                driverHistory.put("dateTime", dateTimeMillis);

                Map<String, Object> riderHistory = new HashMap<>();
                riderHistory.put("role", riderRole);
                riderHistory.put("startLocation", startLocation);
                riderHistory.put("endLocation", endLocation);
                riderHistory.put("dateTime", dateTimeMillis);

                DatabaseReference rideHistoryRef = FirebaseDatabase.getInstance().getReference("ride_history");

                // Save for both driver and rider
                rideHistoryRef.child(driverId).child(rideId).setValue(driverHistory);
                rideHistoryRef.child(riderId).child(rideId).setValue(riderHistory);

                Toast.makeText(LoadingActivity.this, "Ride saved to history!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("saveRideToHistory", "Failed to fetch proposal: " + error.getMessage());
            }
        });
    }



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
                })
                .addOnFailureListener(e -> {
                    Log.e("LoadingActivity", "Failed to update points", e);
                });
    }

    private void removeCompletedRides() {
        DatabaseReference acceptedRidesRef = FirebaseDatabase.getInstance().getReference("accepted_rides");

        // Query to get all accepted rides
        acceptedRidesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                // Loop through all drivers' accepted rides
                for (DataSnapshot driverSnapshot : snapshot.getChildren()) {
                    // Loop through all the rides for each driver
                    for (DataSnapshot rideSnapshot : driverSnapshot.getChildren()) {
                        Ride ride = rideSnapshot.getValue(Ride.class);

                        if (ride != null) {
                            Long dateTimeMillis = ride.getDateTime();

                            if (dateTimeMillis != null) {
                                long currentTimeMillis = System.currentTimeMillis();

                                // If the ride date is in the past, remove it
                                if (dateTimeMillis <= currentTimeMillis) {
                                    String proposalId = rideSnapshot.getKey();
                                    String riderId = ride.getRiderId();
                                    String driverId = ride.getDriverId();

                                    if (proposalId != null && riderId != null && driverId != null) {
                                        // Remove the ride from accepted_rides for both driver and rider
                                        acceptedRidesRef.child(driverId).child(proposalId).removeValue();
                                        acceptedRidesRef.child(riderId).child(proposalId).removeValue();
                                        Log.d("removeCompletedRides", "Removed completed ride: " + proposalId);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("removeCompletedRides", "Failed to fetch accepted rides", error.toException());
            }
        });
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}