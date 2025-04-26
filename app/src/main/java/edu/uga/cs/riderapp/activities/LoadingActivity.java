package edu.uga.cs.riderapp.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.uga.cs.riderapp.R;
import edu.uga.cs.riderapp.models.Proposal;

public class LoadingActivity extends AppCompatActivity {

    private String proposalId;
    private boolean isDriver;
    private String currentUserId;

    private ProgressBar progressBar;
    private TextView loadingText, subText;
    private LinearLayout driverAcceptedLayout, riderAcceptedLayout;
    private TextView driverMatchDetails, riderMatchDetails;
    private LinearLayout driverButtonContainer, riderButtonContainer;
    private Button driverAcceptButton, driverRejectButton;
    private Button riderAcceptButton, riderRejectButton;
    private Button cancelButton;
    private LinearLayout dualButtonContainer;
    private Button backButton, completeButton;

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
        subText = findViewById(R.id.subText);
        driverAcceptedLayout = findViewById(R.id.driverAcceptedLayout);
        riderAcceptedLayout = findViewById(R.id.riderAcceptedLayout);
        driverMatchDetails = findViewById(R.id.driverMatchDetails);
        riderMatchDetails = findViewById(R.id.riderMatchDetails);
        cancelButton = findViewById(R.id.cancelButton);
        dualButtonContainer = findViewById(R.id.dualButtonContainer);
        backButton = findViewById(R.id.backButton);
        completeButton = findViewById(R.id.completeButton);
        driverButtonContainer = findViewById(R.id.driverButtonContainer);
        riderButtonContainer = findViewById(R.id.riderButtonContainer);
        driverAcceptButton = findViewById(R.id.driverAcceptButton);
        driverRejectButton = findViewById(R.id.driverRejectButton);
        riderAcceptButton = findViewById(R.id.riderAcceptButton);
        riderRejectButton = findViewById(R.id.riderRejectButton);
    }

    private void setupButtonListeners() {
        cancelButton.setOnClickListener(v -> cancelProposal());
        backButton.setOnClickListener(v -> navigateToHome());
        completeButton.setOnClickListener(v -> markRideCompleted());
        driverAcceptButton.setOnClickListener(v -> acceptProposal());
        driverRejectButton.setOnClickListener(v -> rejectProposal());
        riderAcceptButton.setOnClickListener(v -> acceptProposal());
        riderRejectButton.setOnClickListener(v -> rejectProposal());
    }

    private void setupDatabaseListener() {
        proposalRef = FirebaseDatabase.getInstance().getReference("accepted_rides").child(proposalId);

        proposalRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Proposal proposal = snapshot.getValue(Proposal.class);
                    if (proposal != null && "accepted".equals(proposal.getStatus())) {
                        proposalAccepted(proposal);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(LoadingActivity.this, "Failed to load proposal info", Toast.LENGTH_SHORT).show();
            }
        });

        proposalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Proposal proposal = snapshot.getValue(Proposal.class);
                    if (proposal != null) {
                        // Handle all possible statuses
                        switch (proposal.getStatus()) {
                            case "accepted":
                                proposalAccepted(proposal);
                                break;
                            case "pending":
                                resetToWaitingState();
                                break;
                            case "cancelled":
                            case "completed":
                                navigateToHome();
                                break;
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

    private void proposalAccepted(Proposal proposal) {
        progressBar.setVisibility(View.GONE);
        loadingText.setVisibility(View.GONE);
        subText.setVisibility(View.GONE);

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        if (isDriver) {
            driverAcceptedLayout.setVisibility(View.VISIBLE);


            usersRef.child(proposal.getRiderId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String riderName = snapshot.child("name").getValue(String.class);

                    driverMatchDetails.setText(String.format(
                            "Rider: %s\nPickup: %s\nDropoff: %s",
                            riderName != null ? riderName : "Unknown",
                            proposal.getStartLocation(),
                            proposal.getEndLocation()
                    ));
                }

                @Override
                public void onCancelled(DatabaseError error) { }
            });

        } else {
            riderAcceptedLayout.setVisibility(View.VISIBLE);


            usersRef.child(proposal.getDriverId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String driverName = snapshot.child("name").getValue(String.class);
                    String carModel = snapshot.child("carModel").getValue(String.class); // optional if you want car

                    riderMatchDetails.setText(String.format(
                            "Driver: %s\nCar: %s\nPickup: %s\nDropoff: %s",
                            driverName != null ? driverName : "Unknown",
                            carModel != null ? carModel : "Unknown",
                            proposal.getStartLocation(),
                            proposal.getEndLocation()
                    ));
                }
                @Override
                public void onCancelled(DatabaseError error) { }
            });
        }
        cancelButton.setVisibility(View.GONE);
        dualButtonContainer.setVisibility(View.VISIBLE);
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

                    Toast.makeText(LoadingActivity.this, "Ride completed!", Toast.LENGTH_SHORT).show();
                    navigateToHome();
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

    private void acceptProposal() {
        if (proposalRef != null) {
            proposalRef.child("status").setValue("accepted")
                    .addOnSuccessListener(aVoid -> {
                        // Hide accept/reject buttons and show the dual buttons at bottom
                        if (isDriver) {
                            driverButtonContainer.setVisibility(View.GONE);
                        } else {
                            riderButtonContainer.setVisibility(View.GONE);
                        }
                        dualButtonContainer.setVisibility(View.VISIBLE);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(LoadingActivity.this, "Failed to accept proposal", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void rejectProposal() {
        if (proposalRef != null) {
            proposalRef.child("status").setValue("pending")
                    .addOnSuccessListener(aVoid -> {
                        // Reset to waiting state
                        resetToWaitingState();
                        Toast.makeText(LoadingActivity.this, "Proposal rejected", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(LoadingActivity.this, "Failed to reject proposal", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void resetToWaitingState() {
        progressBar.setVisibility(View.VISIBLE);
        loadingText.setVisibility(View.VISIBLE);
        loadingText.setText("Waiting for a match...");
        subText.setVisibility(View.VISIBLE);

        driverAcceptedLayout.setVisibility(View.GONE);
        riderAcceptedLayout.setVisibility(View.GONE);
        driverButtonContainer.setVisibility(View.GONE);
        riderButtonContainer.setVisibility(View.GONE);

        cancelButton.setVisibility(View.VISIBLE);
        dualButtonContainer.setVisibility(View.GONE);
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

}