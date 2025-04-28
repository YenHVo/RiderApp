package edu.uga.cs.riderapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
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
import java.util.List;

import edu.uga.cs.riderapp.R;
import edu.uga.cs.riderapp.activities.HomeActivity;
import edu.uga.cs.riderapp.activities.LoadingActivity;
import edu.uga.cs.riderapp.fragments.placeholder.PlaceholderContent;
import edu.uga.cs.riderapp.models.Proposal;
import edu.uga.cs.riderapp.models.User;

/**
 * A fragment representing a list of Items.
 */
public class ProposalListFragment extends Fragment {

    private static final String ARG_USER_ID = "user_id";
    private RecyclerView recyclerView;
    private ProposalRecyclerViewAdapter adapter;
    private List<Proposal> proposals = new ArrayList<>();
    private String currentUserId;

    private DatabaseReference proposalsRef;
    private ValueEventListener proposalsListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProposalListFragment() {
    }

    @SuppressWarnings("unused")
    public static ProposalListFragment newInstance(String userId) {
        ProposalListFragment fragment = new ProposalListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentUserId = getArguments().getString(ARG_USER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_proposal_list, container, false);

        recyclerView = view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ProposalRecyclerViewAdapter(proposals, new ProposalRecyclerViewAdapter.OnProposalClickListener() {
            public void onAcceptClick(Proposal proposal) {
                /*
                boolean isDriver = "request".equals(proposal.getType());

                DatabaseReference proposalRef = FirebaseDatabase.getInstance()
                        .getReference("proposals")
                        .child(proposal.getProposalId());

                if (isDriver) {
                    proposalRef.child("driverStatus").setValue("accepted");
                    proposalRef.child("driverId").setValue(currentUserId);
                } else {
                    proposalRef.child("riderStatus").setValue("accepted");
                    proposalRef.child("riderId").setValue(currentUserId);
                }
                checkBothUsersConfirmed(proposal);
                Intent intent = new Intent(getActivity(), LoadingActivity.class);
                intent.putExtra("proposalId", proposal.getProposalId());
                intent.putExtra("isDriver", isDriver);
                startActivity(intent);
            }*/

                boolean isDriver = "request".equals(proposal.getType());
                DatabaseReference proposalRef = FirebaseDatabase.getInstance()
                        .getReference("proposals")
                        .child(proposal.getProposalId());

                // Get current user's name from Firebase Auth
                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                String currentUserName = currentFirebaseUser != null ?
                        (currentFirebaseUser.getDisplayName() != null ?
                                currentFirebaseUser.getDisplayName() :
                                currentFirebaseUser.getEmail() != null ?
                                        currentFirebaseUser.getEmail().split("@")[0] : "User") :
                        "User";

                if (isDriver) {
                    proposalRef.child("driverStatus").setValue("accepted");
                    proposalRef.child("driverId").setValue(currentUserId);
                    proposalRef.child("driverName").setValue(currentUserName);  // Add this line
                } else {
                    proposalRef.child("riderStatus").setValue("accepted");
                    proposalRef.child("riderId").setValue(currentUserId);
                    proposalRef.child("riderName").setValue(currentUserName);  // Add this line
                }

                checkBothUsersConfirmed(proposal);
                Intent intent = new Intent(getActivity(), LoadingActivity.class);
                intent.putExtra("proposalId", proposal.getProposalId());
                intent.putExtra("isDriver", isDriver);
                startActivity(intent);
            }

            public void onCancelClick(Proposal proposal, View actionButtonsLayout) {
                actionButtonsLayout.setVisibility(View.GONE);
            }
        });

        recyclerView.setAdapter(adapter);
        loadProposalsFromFirebase();

        return view;
    }

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


    private void loadProposalsFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;  // User is not logged in, don't try to load proposals
        }

        // If proposalsRef or proposalsListener is not already set, set them up
        if (proposalsRef == null) {
            proposalsRef = FirebaseDatabase.getInstance().getReference("proposals");
        }

        // Add the listener only if it hasn't been added yet
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
                        String userId = proposalSnap.child("userId").getValue(String.class);

                        Proposal proposal = new Proposal();
                        proposal.setProposalId(proposalId);
                        proposal.setType(type);
                        proposal.setStartLocation(startLocation);
                        proposal.setEndLocation(endLocation);

                        if ("offer".equals(type)) {
                            String carModel = proposalSnap.child("carModel").getValue(String.class);
                            Integer seats = proposalSnap.child("seatsAvailable").getValue(Integer.class);
                            proposal.setCar(carModel);
                            proposal.setAvailableSeats(seats != null ? seats : 0);
                        }

                        String driverStatus = proposalSnap.child("driverStatus").getValue(String.class);
                        String riderStatus = proposalSnap.child("riderStatus").getValue(String.class);

                        proposal.setDriverStatus(driverStatus != null ? driverStatus : "pending");
                        proposal.setRiderStatus(riderStatus != null ? riderStatus : "pending");

                        if ("pending".equals(proposal.getDriverStatus()) && "pending".equals(proposal.getRiderStatus())) {
                            proposals.add(proposal);
                            if (userId != null && !userId.isEmpty()) {
                                fetchUserById(userId, proposal);
                            } else {
                                Log.e("ProposalListFragment", "Proposal missing userId: " + proposal.getProposalId());
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Only show the Toast if user is logged in (i.e., not null)
                    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                        Toast.makeText(getContext(), "Failed to load proposals.", Toast.LENGTH_SHORT).show();
                    }
                }
            };
            proposalsRef.addValueEventListener(proposalsListener);
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        // Remove the listener when the fragment is stopped (including on logout)
        if (proposalsRef != null && proposalsListener != null) {
            proposalsRef.removeEventListener(proposalsListener);
        }
    }

    private void fetchUserById(String userId, Proposal proposal) {
        if (userId == null) {
            Log.e("ProposalListFragment", "User ID is null, cannot fetch user data.");
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);

                    if ("offer".equals(proposal.getType())) {
                        proposal.setDriverId(userId);
                    } else {
                        proposal.setRiderId(userId);
                    }

                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("ProposalListFragment", "User not found in database for ID: " + userId);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("ProposalListFragment", "Database error: " + error.getMessage());
            }
        });
    }

    private void fetchUserDetailsAndStartActivity(String driverId, String riderId, Proposal proposal) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User driver = snapshot.getValue(User.class);

                usersRef.child(riderId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot riderSnapshot) {
                        User rider = riderSnapshot.getValue(User.class);

                        Intent intent = new Intent(getActivity(), LoadingActivity.class);
                        intent.putExtra("driverName", driver != null ? driver.getName() : "N/A");
                        intent.putExtra("riderName", rider != null ? rider.getName() : "N/A");

                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e("ProposalListFragment", "Failed to fetch rider details: " + error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("ProposalListFragment", "Failed to fetch driver details: " + error.getMessage());
            }
        });
    }

}

