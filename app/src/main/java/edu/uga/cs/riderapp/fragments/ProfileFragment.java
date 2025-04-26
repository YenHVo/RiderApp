package edu.uga.cs.riderapp.fragments;



import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    private TextView userNameTextView;
    private RecyclerView recyclerView;
    private RideHistoryAdapter adapter;
    private List<RideHistory> rideHistoryList;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        userNameTextView = view.findViewById(R.id.textViewUserName);
        recyclerView = view.findViewById(R.id.recyclerViewRideHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        rideHistoryList = new ArrayList<>();
        adapter = new RideHistoryAdapter(rideHistoryList);
        recyclerView.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("ride_history");

        loadUserProfile();
        loadRideHistory();

        return view;
    }

    private void loadUserProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            userNameTextView.setText(displayName != null ? displayName : "User");
        }
    }

    private void loadRideHistory() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();

        // First fetch where user is driver
        databaseReference.orderByChild("driverId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        rideHistoryList.clear();
                        for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                            RideHistory ride = rideSnapshot.getValue(RideHistory.class);
                            if (ride != null) {
                                rideHistoryList.add(ride);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

        // Then fetch where user is rider
        databaseReference.orderByChild("riderId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                            RideHistory ride = rideSnapshot.getValue(RideHistory.class);
                            if (ride != null) {
                                rideHistoryList.add(ride);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
