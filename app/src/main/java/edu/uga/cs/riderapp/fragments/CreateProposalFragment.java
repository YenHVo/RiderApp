package edu.uga.cs.riderapp.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import edu.uga.cs.riderapp.R;
import edu.uga.cs.riderapp.models.Proposal;
import edu.uga.cs.riderapp.models.User;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateProposalFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateProposalFragment extends Fragment {

    private RadioGroup proposalTypeGroup;
    private EditText startLocationEdit;
    private EditText destinationEdit;
    private EditText carModelEdit;
    private EditText availableSeatsEdit;
    private LinearLayout carDetailsLayout;
    private Button submitButton;

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
        destinationEdit = view.findViewById(R.id.destinationEdit);
        carModelEdit = view.findViewById(R.id.carModelEdit);
        availableSeatsEdit = view.findViewById(R.id.availableSeatsEdit);
        carDetailsLayout = view.findViewById(R.id.carDetailsLayout);
        submitButton = view.findViewById(R.id.submitButton);

        // Handle proposal type change
        proposalTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.offerRadio) {
                carDetailsLayout.setVisibility(View.VISIBLE);
            } else {
                carDetailsLayout.setVisibility(View.GONE);
            }
        });

        // Submit button click handler
        submitButton.setOnClickListener(v -> createProposal());
        return view;
    }

    private void createProposal() {
        // Get current user
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get basic fields
        String startLocation = startLocationEdit.getText().toString().trim();
        String destination = destinationEdit.getText().toString().trim();

        if (startLocation.isEmpty() || destination.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isOffer = proposalTypeGroup.getCheckedRadioButtonId() == R.id.offerRadio;

        if (isOffer) {
            // Validate offer fields
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

            // Create ride offer
            Proposal proposal = new Proposal(
                    "offer",
                    startLocation,
                    destination,
                    currentUser,
                    carModel,
                    availableSeats
            );
            saveProposal(proposal);
            Toast.makeText(getContext(), "Ride offer created!", Toast.LENGTH_SHORT).show();
        } else {
            // Create ride request
            Proposal proposal = new Proposal(
                    "request",
                    startLocation,
                    destination,
                    currentUser
            );
            saveProposal(proposal);
            Toast.makeText(getContext(), "Ride request created!", Toast.LENGTH_SHORT).show();
        }
        clearForm();
    }

    private User getCurrentUser() {
        // todo: get the current user somehow
        User testUser = new User("test@example.com", "Test User");
        testUser.setName("Test User");
        return testUser;
    }

    private void saveProposal(Proposal proposal) {
        DatabaseReference proposalsRef = FirebaseDatabase.getInstance().getReference("proposals");
        String proposalId = proposalsRef.push().getKey();

        if (proposalId != null) {
            proposalsRef.child(proposalId).setValue(proposal)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Proposal saved to Firebase", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }


    private void clearForm() {
        startLocationEdit.setText("");
        destinationEdit.setText("");
        carModelEdit.setText("");
        availableSeatsEdit.setText("");
    }
}