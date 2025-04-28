package edu.uga.cs.riderapp.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import edu.uga.cs.riderapp.R;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import edu.uga.cs.riderapp.fragments.RideHistoryAdapter;
import edu.uga.cs.riderapp.models.RideHistory;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private RecyclerView recyclerView;
    private RideHistoryAdapter adapter;
    private List<RideHistory> rideHistoryList;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private TextView noHistoryTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize UI elements
        recyclerView = view.findViewById(R.id.recyclerViewRideHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        rideHistoryList = new ArrayList<>();
        adapter = new RideHistoryAdapter(rideHistoryList);
        recyclerView.setAdapter(adapter);

        noHistoryTextView = view.findViewById(R.id.textViewNoHistory);

        // Initialize Firebase Auth and Database reference
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("ride_history");

        // Load ride history
        loadRideHistory();

        return view;
    }

    private void loadRideHistory() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();
        rideHistoryList.clear(); // Clear the previous list

        // Query rides where the user is the driver
        databaseReference.orderByChild("driverId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                            RideHistory ride = rideSnapshot.getValue(RideHistory.class);
                            if (ride != null && "completed".equals(ride.getStatus())) {
                                if (!rideHistoryList.contains(ride)) {
                                    rideHistoryList.add(ride);
                                }
                            }
                        }
                        checkIfHistoryIsEmpty();
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("ProfileFragment", "Failed to load ride history: " + error.getMessage());
                    }
                });

        // Query rides where the user is the rider
        databaseReference.orderByChild("riderId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                            RideHistory ride = rideSnapshot.getValue(RideHistory.class);
                            if (ride != null && "completed".equals(ride.getStatus())) {
                                if (!rideHistoryList.contains(ride)) {
                                    rideHistoryList.add(ride);
                                }
                            }
                        }
                        checkIfHistoryIsEmpty();
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("ProfileFragment", "Failed to load ride history: " + error.getMessage());
                    }
                });
    }


    private void checkIfHistoryIsEmpty() {
        // Show/hide the "no history" message
        if (rideHistoryList.isEmpty()) {
            noHistoryTextView.setVisibility(View.VISIBLE);
        } else {
            noHistoryTextView.setVisibility(View.GONE);
        }
    }
}
