package edu.uga.cs.riderapp.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import edu.uga.cs.riderapp.R;
import edu.uga.cs.riderapp.activities.HomeActivity;
import edu.uga.cs.riderapp.activities.LoadingActivity;
import edu.uga.cs.riderapp.activities.MainActivity;
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
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateProposalFragment#newInstance} factory method to
 * create an instance of this fragment.
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


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CreateProposalFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CreateProposalFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateProposalFragment newInstance(String param1, String param2) {
        CreateProposalFragment fragment = new CreateProposalFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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

        selectedDateTime = Calendar.getInstance();  // Initialize

        selectDateButton.setOnClickListener(v -> showDatePickerDialog());
        selectTimeButton.setOnClickListener(v -> showTimePickerDialog());


        // Handle proposal type change
        proposalTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.offerRadio) {
                carDetailsLayout.setVisibility(View.VISIBLE);
            } else {
                carDetailsLayout.setVisibility(View.GONE);
            }
        });


        submitButton.setOnClickListener(v -> createProposal());
        return view;
    }

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

                    // Create the proposal (rider)
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
            // Driver creating a ride offer
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

            // Create the proposal (driver)
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

            // Check that required fields are not null
            if (proposal.getType().equals("offer") && proposal.getDriverId() == null) {
                Toast.makeText(getContext(), "Offer missing driverId!", Toast.LENGTH_SHORT).show();
                Log.e("CreateProposal", "Offer missing driverId");
                return;
            }
            if (proposal.getType().equals("request") && proposal.getRiderId() == null) {
                Toast.makeText(getContext(), "Request missing riderId!", Toast.LENGTH_SHORT).show();
                Log.e("CreateProposal", "Request missing riderId");
                return;
            }

            saveProposal(proposal, isOffer);

            Toast.makeText(getContext(), "Ride offer created!", Toast.LENGTH_SHORT).show();
            clearForm();
        }
    }



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

    private void updateDateTimeDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String formatted = sdf.format(selectedDateTime.getTime());
        dateTimeDisplay.setText(formatted);
    }

    private User getCurrentUser() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            return new User(
                    firebaseUser.getUid(),
                    firebaseUser.getEmail(),
                    firebaseUser.getDisplayName() != null ?
                            firebaseUser.getDisplayName() :
                            firebaseUser.getEmail() != null ?
                                    firebaseUser.getEmail().split("@")[0] : "User",
                    0,
                    new Date()
            );
        }
        return null;
    }

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

        private void clearForm() {
        startLocationEdit.setText("");
        endLocationEdit.setText("");
        carModelEdit.setText("");
        availableSeatsEdit.setText("");
    }
}