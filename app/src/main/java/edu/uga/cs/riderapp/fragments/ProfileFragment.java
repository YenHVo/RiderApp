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

import edu.uga.cs.riderapp.models.RideHistory;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays the user's ride history in a RecyclerView.
 * Fetches data from Firebase Realtime Database and handles UI state when no data is present.
 */
public class ProfileFragment extends Fragment {

    private RecyclerView recyclerView;
    private RideHistoryAdapter adapter;
    private List<RideHistory> rideHistoryList;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private TextView noHistoryTextView;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Inflates the fragment layout, sets up UI and Firebase, and loads ride history.
     *
     * @param inflater The LayoutInflater object that can be used to inflate views.
     * @param container The parent view that the fragment UI should be attached to.
     * @param savedInstanceState A saved state if any.
     * @return The root view of this fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize UI elements
        recyclerView = view.findViewById(R.id.recyclerViewRideHistory);
        noHistoryTextView = view.findViewById(R.id.textViewNoHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        rideHistoryList = new ArrayList<>();
        adapter = new RideHistoryAdapter(rideHistoryList);
        recyclerView.setAdapter(adapter);

        // Initialize Firebase Auth and Database reference
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("ride_history");

        // Load ride history
        loadRideHistory();

        return view;
    }

    /**
     * Loads the ride history of the currently logged-in user from Firebase Realtime Database.
     * Updates the adapter and UI based on the data retrieved.
     */
    private void loadRideHistory() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();
        rideHistoryList.clear();

        // Query the ride history for the current user
        databaseReference.child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot rideSnapshot : snapshot.getChildren()) {
                            RideHistory ride = rideSnapshot.getValue(RideHistory.class);
                            if (ride != null) {
                                rideHistoryList.add(ride);
                            }
                        }
                        checkIfHistoryIsEmpty();
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    /**
     * Checks if ride history is empty and shows/hides the corresponding message.
     */
    private void checkIfHistoryIsEmpty() {
        // Show/hide the "no history" message
        if (rideHistoryList.isEmpty()) {
            noHistoryTextView.setVisibility(View.VISIBLE);
        } else {
            noHistoryTextView.setVisibility(View.GONE);
        }
    }
}
