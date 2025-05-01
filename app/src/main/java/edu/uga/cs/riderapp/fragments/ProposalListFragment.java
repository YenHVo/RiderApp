package edu.uga.cs.riderapp.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uga.cs.riderapp.R;
import edu.uga.cs.riderapp.activities.LoadingActivity;
import edu.uga.cs.riderapp.models.Proposal;

/**
 * Fragment that displays a list of pending ride proposals for either riders or drivers.
 * Handles accepting and confirming proposals through Firebase.
 */
public class ProposalListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProposalRecyclerViewAdapter adapter;
    private List<Proposal> proposals = new ArrayList<>();
    private DatabaseReference proposalsRef;
    private ValueEventListener proposalsListener;

    public ProposalListFragment() {
        // Required empty public constructor
    }

    /**
     * Sets up the RecyclerView and its adapter, and initializes Firebase listeners.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_proposal_list, container, false);

        recyclerView = view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ProposalRecyclerViewAdapter(proposals, new ProposalRecyclerViewAdapter.OnProposalClickListener() {
            /**
             * Handles the Accept button logic for a proposal.
             */
            @Override
            public void onAcceptClick(Proposal proposal) {
                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentFirebaseUser == null) {
                    Toast.makeText(getContext(), "You are not logged in!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String currentUserId = currentFirebaseUser.getUid();
                String currentUserName = currentFirebaseUser.getDisplayName() != null ?
                        currentFirebaseUser.getDisplayName() :
                        (currentFirebaseUser.getEmail() != null ? currentFirebaseUser.getEmail().split("@")[0] : "User");

                boolean isDriver = "request".equals(proposal.getType());

                DatabaseReference proposalRef = FirebaseDatabase.getInstance()
                        .getReference("proposals")
                        .child(proposal.getProposalId());

                Map<String, Object> updates = new HashMap<>();
                if (isDriver) {
                    // Driver accepting a rider request
                    updates.put("driverId", currentUserId);
                    updates.put("driverName", currentUserName);
                    updates.put("driverStatus", "accepted");
                    if (proposal.getCar() != null) updates.put("car", proposal.getCar());
                    if (proposal.getAvailableSeats() > 0) updates.put("availableSeats", proposal.getAvailableSeats());
                } else {
                    // Rider accepting a driver offer
                    updates.put("riderId", currentUserId);
                    updates.put("riderName", currentUserName);
                    updates.put("riderStatus", "accepted");
                }

                proposalRef.updateChildren(updates)
                        .addOnSuccessListener(aVoid -> {
                            checkBothUsersConfirmed(proposal);

                            // Navigate to loading activity
                            Intent intent = new Intent(getActivity(), LoadingActivity.class);
                            intent.putExtra("proposalId", proposal.getProposalId());
                            intent.putExtra("isDriver", isDriver);
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Failed to accept proposal.", Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onCancelClick(Proposal proposal, View actionButtonsLayout) {
                actionButtonsLayout.setVisibility(View.GONE);
            }
        });

        recyclerView.setAdapter(adapter);
        loadProposalsFromFirebase();

        return view;
    }

    /**
     * Checks if both users have accepted and confirms the proposal.
     */
    private void checkBothUsersConfirmed(Proposal proposal) {
        DatabaseReference proposalRef = FirebaseDatabase.getInstance().getReference("proposals").child(proposal.getProposalId());

        proposalRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String driverStatus = snapshot.child("driverStatus").getValue(String.class);
                String riderStatus = snapshot.child("riderStatus").getValue(String.class);

                // If both driver and rider have accepted, update the status to 'confirmed'
                if ("accepted".equals(driverStatus) && "accepted".equals(riderStatus)) {
                    proposalRef.child("status").setValue("confirmed");

                    // Remove the proposal from the database (or add it to a history list)
                    proposalRef.removeValue();

                    Toast.makeText(getContext(), "Proposal confirmed!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("ProposalListFragment", "Database error: " + error.getMessage());
            }
        });
    }

    /**
     * Loads pending proposals from Firebase and populates the list.
     */
    private void loadProposalsFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        if (proposalsRef == null) {
            proposalsRef = FirebaseDatabase.getInstance().getReference("proposals");
        }

        if (proposalsListener == null) {
            proposalsListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    proposals.clear();

                    for (DataSnapshot proposalSnap : snapshot.getChildren()) {
                        String proposalId = proposalSnap.getKey();
                        String type = proposalSnap.child("type").getValue(String.class);
                        String startLocation = proposalSnap.child("startLocation").getValue(String.class);
                        String endLocation = proposalSnap.child("endLocation").getValue(String.class);

                        // Extract dateTime from Firebase
                        Object dateTimeObject = proposalSnap.child("dateTime").getValue();
                        long dateTimeMillis;
                        if (dateTimeObject instanceof Long) {
                            dateTimeMillis = (Long) dateTimeObject;
                        } else if (dateTimeObject instanceof Double) {
                            dateTimeMillis = ((Double) dateTimeObject).longValue();
                        } else {
                            Log.e("ProposalListFragment", "Unexpected dateTime format: " + dateTimeObject);
                            dateTimeMillis = 0L; // fallback
                        }

                        Proposal proposal = new Proposal();

                        if (type == null || (!type.equals("offer") && !type.equals("request"))) {
                            Log.e("ProposalListFragment", "Invalid or missing type at key: " + proposalId);
                            continue;
                        }
                        proposal.setProposalId(proposalId);
                        proposal.setType(type);
                        proposal.setStartLocation(startLocation);
                        proposal.setEndLocation(endLocation);
                        proposal.setDateTime(dateTimeMillis);

                        // Handle offer-specific data
                        if ("offer".equals(type)) {
                            String carModel = proposalSnap.child("carModel").getValue(String.class);
                            Integer seats = proposalSnap.child("seatsAvailable").getValue(Integer.class);
                            String driverId = proposalSnap.child("driverId").getValue(String.class);

                            proposal.setCar(carModel);
                            proposal.setAvailableSeats(seats != null ? seats : 0);
                            proposal.setDriverId(driverId);

                            if (driverId == null) {
                                Log.e("ProposalListFragment", "Offer missing driverId at key: " + proposalId);
                                continue;
                            }
                        } else if ("request".equals(type)) {
                            // Handle request-specific data
                            String riderId = proposalSnap.child("riderId").getValue(String.class);
                            proposal.setRiderId(riderId);

                            if (riderId == null) {
                                Log.e("ProposalListFragment", "Request missing riderId at key: " + proposalId);
                                continue;
                            }
                        }

                        // Driver/Rider status
                        String driverStatus = proposalSnap.child("driverStatus").getValue(String.class);
                        String riderStatus = proposalSnap.child("riderStatus").getValue(String.class);

                        proposal.setDriverStatus(driverStatus != null ? driverStatus : "pending");
                        proposal.setRiderStatus(riderStatus != null ? riderStatus : "pending");

                        // Add only if both statuses are still pending
                        if ("pending".equals(proposal.getDriverStatus()) && "pending".equals(proposal.getRiderStatus())) {
                            proposals.add(proposal);
                        }
                    }

                    if (proposals != null && !proposals.isEmpty()) {
                        proposals.sort((p1, p2) -> Long.compare(p1.getDateTime(), p2.getDateTime()));
                    } else {
                        Log.d("ProposalListFragment", "Proposals list is empty or null");
                    }

                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        Toast.makeText(getContext(), "Failed to load proposals.", Toast.LENGTH_SHORT).show();
                    }
                }
            };

            proposalsRef.addValueEventListener(proposalsListener);
        }
    }

    /**
     * Removes the Firebase listener when the fragment is stopped.
     */
    @Override
    public void onStop() {
        super.onStop();
        // Remove the listener when the fragment is stopped (including on logout)
        if (proposalsRef != null && proposalsListener != null) {
            proposalsRef.removeEventListener(proposalsListener);
        }
    }

    /**
     * Cleans up the RecyclerView and Firebase listeners on view destruction.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (proposalsRef != null && proposalsListener != null) {
            proposalsRef.removeEventListener(proposalsListener);
            proposalsListener = null;
        }
        proposals.clear();
        adapter.notifyDataSetChanged();
    }
}

