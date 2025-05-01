package edu.uga.cs.riderapp.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import edu.uga.cs.riderapp.R;
import edu.uga.cs.riderapp.activities.LoadingActivity;
import edu.uga.cs.riderapp.models.Proposal;
import edu.uga.cs.riderapp.models.User;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment allowing users to create a ride proposal, either as a rider (request) or driver (offer).
 * Handles form input, validation, and saving proposal data to Firebase Realtime Database.
 */
public class CreateProposalFragment extends Fragment {

    private RadioGroup proposalTypeGroup;
    private EditText startLocationEdit;
    private EditText endLocationEdit;
    private EditText carModelEdit;
    private EditText availableSeatsEdit;
    private LinearLayout carDetailsLayout;
    private Button submitButton;

    private Button selectDateButton;
    private Button selectTimeButton;
    private TextView dateTimeDisplay;
    private Calendar selectedDateTime;
    private User currentUser;

    public CreateProposalFragment() {
        // Required empty public constructor
    }


    /**
     * Inflates the fragment layout, initializes UI components,
     * sets listeners, and prepares the proposal creation form.
     *
     * @param inflater LayoutInflater to inflate the view.
     * @param container ViewGroup container.
     * @param savedInstanceState Bundle of saved instance state.
     * @return the root view of the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_proposal, container, false);

        // Initialize views
        proposalTypeGroup = view.findViewById(R.id.proposalTypeGroup);
        startLocationEdit = view.findViewById(R.id.startLocationEdit);
        endLocationEdit = view.findViewById(R.id.endLocationEdit);
        carModelEdit = view.findViewById(R.id.carModelEdit);
        availableSeatsEdit = view.findViewById(R.id.availableSeatsEdit);
        carDetailsLayout = view.findViewById(R.id.carDetailsLayout);
        submitButton = view.findViewById(R.id.submitButton);
        selectDateButton = view.findViewById(R.id.selectDateButton);
        selectTimeButton = view.findViewById(R.id.selectTimeButton);
        dateTimeDisplay = view.findViewById(R.id.dateTimeDisplay);

        selectedDateTime = Calendar.getInstance();

        // Handle proposal type change
        proposalTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.offerRadio) {
                carDetailsLayout.setVisibility(View.VISIBLE);
            } else {
                carDetailsLayout.setVisibility(View.GONE);
            }
        });

        // Set Listeners
        selectDateButton.setOnClickListener(v -> showDatePickerDialog());
        selectTimeButton.setOnClickListener(v -> showTimePickerDialog());
        submitButton.setOnClickListener(v -> createProposal());

        return view;
    }

    /**
     * Creates a ride proposal after validating input fields and user eligibility.
     * Sends the proposal data to Firebase.
     */
    /*
    private void createProposal() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String startLocation = startLocationEdit.getText().toString().trim();
        String endLocation = endLocationEdit.getText().toString().trim();

        if (startLocation.isEmpty() || endLocation.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDateTime == null) {
            Toast.makeText(getContext(), "Please select a valid date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        long dateTimeMillis = selectedDateTime.getTimeInMillis();
        int checkedRadioButtonId = proposalTypeGroup.getCheckedRadioButtonId();
        if (checkedRadioButtonId == -1) {
            Toast.makeText(getContext(), "Please select a proposal type (offer or request)", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isOffer = proposalTypeGroup.getCheckedRadioButtonId() == R.id.offerRadio;
        if (!isOffer) {
            // Request proposal (rider)
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(currentUser.getUserId())
                    .child("points");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Object pointsObj = snapshot.getValue();
                    long points = 0;

                    if (pointsObj instanceof Long) {
                        points = (Long) pointsObj;
                    } else if (pointsObj instanceof Double) {
                        points = ((Double) pointsObj).longValue();
                    } else {
                        Log.e("CreateProposalFragment", "Invalid points data: " + pointsObj);
                        Toast.makeText(getContext(), "Account points are invalid. Contact support.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (points < 100) {
                        Toast.makeText(getContext(), "Not enough points. Give a ride to get more.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Create the request proposal (rider)
                    Proposal proposal = new Proposal(
                            "request",
                            startLocation,
                            endLocation,
                            null,
                            currentUser.getUserId(),
                            null,
                            0,
                            dateTimeMillis
                    );

                    // Ensure riderId exists for requests
                    if (proposal.getRiderId() == null) {
                        Toast.makeText(getContext(), "Request missing riderId!", Toast.LENGTH_SHORT).show();
                        Log.e("CreateProposal", "Request missing riderId");
                        return;
                    }

                    saveProposal(proposal, isOffer);

                    Toast.makeText(getContext(), "Ride request created!", Toast.LENGTH_SHORT).show();
                    clearForm();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Failed to load points", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            // Offer proposal (driver)
            String carModel = carModelEdit.getText().toString().trim();
            String seatsStr = availableSeatsEdit.getText().toString().trim();

            if (carModel.isEmpty() || seatsStr.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all car details", Toast.LENGTH_SHORT).show();
                return;
            }

            int availableSeats;
            try {
                availableSeats = Integer.parseInt(seatsStr);
                if (availableSeats <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Please enter a valid number of seats", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create the offer proposal (driver)
            Proposal proposal = new Proposal(
                    "offer",
                    startLocation,
                    endLocation,
                    currentUser.getUserId(),
                    null,
                    carModel,
                    availableSeats,
                    dateTimeMillis
            );

            // Ensure driverId exists for offers
            if (proposal.getDriverId() == null) {
                Toast.makeText(getContext(), "Offer missing driverId!", Toast.LENGTH_SHORT).show();
                Log.e("CreateProposal", "Offer missing driverId");
                return;
            }

            saveProposal(proposal, isOffer);

            Toast.makeText(getContext(), "Ride offer created!", Toast.LENGTH_SHORT).show();
            clearForm();
        }
    }*/
    private void createProposal() {
        getCurrentUser(currentUser -> {
            if (currentUser == null) {
                Toast.makeText(getContext(), "Failed to fetch user info", Toast.LENGTH_SHORT).show();
                return;
            }

            String startLocation = startLocationEdit.getText().toString().trim();
            String endLocation = endLocationEdit.getText().toString().trim();

            if (startLocation.isEmpty() || endLocation.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedDateTime == null) {
                Toast.makeText(getContext(), "Please select a valid date and time", Toast.LENGTH_SHORT).show();
                return;
            }

            long dateTimeMillis = selectedDateTime.getTimeInMillis();
            int checkedRadioButtonId = proposalTypeGroup.getCheckedRadioButtonId();
            if (checkedRadioButtonId == -1) {
                Toast.makeText(getContext(), "Please select a proposal type (offer or request)", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean isOffer = checkedRadioButtonId == R.id.offerRadio;

            if (!isOffer) {
                // Rider - check points
                DatabaseReference userRef = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(currentUser.getUserId())
                        .child("points");

                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        long points = snapshot.getValue(Long.class) != null ? snapshot.getValue(Long.class) : 0;

                        if (points < 100) {
                            Toast.makeText(getContext(), "Not enough points. Give a ride to earn more.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Proposal proposal = new Proposal(
                                "request",
                                startLocation,
                                endLocation,
                                null,
                                currentUser.getUserId(),
                                null,
                                0,
                                dateTimeMillis
                        );
                        proposal.setRiderName(currentUser.getName());

                        saveProposal(proposal, false);
                        Toast.makeText(getContext(), "Ride request created!", Toast.LENGTH_SHORT).show();
                        clearForm();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to check points", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Driver
                String carModel = carModelEdit.getText().toString().trim();
                String seatsStr = availableSeatsEdit.getText().toString().trim();

                if (carModel.isEmpty() || seatsStr.isEmpty()) {
                    Toast.makeText(getContext(), "Please fill all car details", Toast.LENGTH_SHORT).show();
                    return;
                }

                int availableSeats;
                try {
                    availableSeats = Integer.parseInt(seatsStr);
                    if (availableSeats <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid seat count", Toast.LENGTH_SHORT).show();
                    return;
                }

                Proposal proposal = new Proposal(
                        "offer",
                        startLocation,
                        endLocation,
                        currentUser.getUserId(),
                        null,
                        carModel,
                        availableSeats,
                        dateTimeMillis
                );
                proposal.setDriverName(currentUser.getName());

                saveProposal(proposal, true);
                Toast.makeText(getContext(), "Ride offer created!", Toast.LENGTH_SHORT).show();
                clearForm();
            }
        });
    }

    /**
     * Displays a date picker dialog and updates the calendar object.
     */
    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateTimeDisplay();
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    /**
     * Displays a time picker dialog and updates the calendar object.
     */
    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    updateDateTimeDisplay();
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    /**
     * Formats the selected date/time and displays it in the UI.
     */
    private void updateDateTimeDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String formatted = sdf.format(selectedDateTime.getTime());
        dateTimeDisplay.setText(formatted);
    }

    /**
     * Builds a User object from the currently authenticated Firebase user.
     * @return User object or null if not authenticated.
     */
    private void getCurrentUser(UserCallback callback) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            callback.onUserResult(null);
            return;
        }

        String uid = firebaseUser.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);

                if (name == null || email == null) {
                    callback.onUserResult(null);
                    return;
                }

                User user = new User(uid, email, name, 0, new Date());
                callback.onUserResult(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onUserResult(null);
            }
        });
    }

    public interface UserCallback {
        void onUserResult(User user);
    }

    /**
     * Saves a Proposal object to Firebase and navigates to the loading screen.
     * @param proposal the Proposal to save.
     * @param isDriver whether the user is acting as a driver.
     */
    private void saveProposal(Proposal proposal, boolean isDriver) {
        DatabaseReference proposalsRef = FirebaseDatabase.getInstance().getReference("proposals");
        String proposalId = proposalsRef.push().getKey();
        if (proposalId == null) {
            Toast.makeText(getContext(), "Failed to create proposal ID", Toast.LENGTH_SHORT).show();
            return;
        }

        proposal.setProposalId(proposalId);
        proposalsRef.child(proposalId).setValue(proposal)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Proposal created successfully!", Toast.LENGTH_SHORT).show();
                    clearForm();

                    Intent intent = new Intent(getActivity(), LoadingActivity.class);
                    intent.putExtra("proposalId", proposalId);
                    intent.putExtra("isDriver", isDriver);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Clears all input fields in the form.
     */
    private void clearForm() {
        startLocationEdit.setText("");
        endLocationEdit.setText("");
        carModelEdit.setText("");
        availableSeatsEdit.setText("");
    }
}