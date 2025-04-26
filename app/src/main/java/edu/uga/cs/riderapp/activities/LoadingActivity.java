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
    private Button cancelButton;
    private LinearLayout dualButtonContainer;
    private Button backButton, completeButton;

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
        currentUserId = "Sarah J."; // todo: find a way to find the current user's id
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
    }

    private void setupButtonListeners() {
        cancelButton.setOnClickListener(v -> cancelProposal());
        backButton.setOnClickListener(v -> navigateToHome());
        completeButton.setOnClickListener(v -> markRideCompleted());
    }

    private void setupDatabaseListener() {
        // todo: initialize database references to listen for proposal status changes
        //  for example: when the proposal goes from 'pending' to 'accepted'
        //  in the case of accepted, call function proposalAccepted(proposal)
    }

    private void proposalAccepted(Proposal proposal) {
        progressBar.setVisibility(View.GONE);
        loadingText.setVisibility(View.GONE);
        subText.setVisibility(View.GONE);

        // todo: find a way to display the driver/rider match details with the database
        if (isDriver) {
            driverAcceptedLayout.setVisibility(View.VISIBLE);
            driverMatchDetails.setText(String.format(
                    "Rider: %s\nPickup: %s\nDropoff: %s",
                    proposal.getRider().getName(),
                    proposal.getStartLocation(),
                    proposal.getEndLocation()
            ));
        } else {
            riderAcceptedLayout.setVisibility(View.VISIBLE);
            riderMatchDetails.setText(String.format(
                    "Driver: %s\nCar: %s\nPickup: %s\nDropoff: %s",
                    proposal.getDriver().getName(),
                    proposal.getCar(),
                    proposal.getStartLocation(),
                    proposal.getEndLocation()
            ));
        }
        
        cancelButton.setVisibility(View.GONE);
        dualButtonContainer.setVisibility(View.VISIBLE);
    }

    private void markRideCompleted() {
        // todo: mark that the current user has completed the ride. if the other user (rider/driver)
        //  has not completed the ride, have it display "WAITING FOR OTHER PARTY"
        //  once both have completed the ride, have it update the points accordingly
    }

    private void cancelProposal() {
        // todo: change the proposal status in the database to 'cancelled'
        //  call function navigateHome
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

}