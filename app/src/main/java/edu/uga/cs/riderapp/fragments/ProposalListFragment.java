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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import edu.uga.cs.riderapp.R;
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
                boolean isDriver = "request".equals(proposal.getType());

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

    private void loadProposalsFromFirebase() {
        DatabaseReference proposalsRef = FirebaseDatabase.getInstance().getReference("proposals");

        proposalsRef.addListenerForSingleValueEvent(new ValueEventListener() {
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

                    fetchUserById(userId, proposal);
                    proposals.add(proposal);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load proposals.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserById(String userId, Proposal proposal) {
        if (userId == null) {
            Log.e("ProposalListFragment", "User ID is null, cannot fetch user data.");
            return; // Exit the method if userId is null
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
}

