package edu.uga.cs.riderapp.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import edu.uga.cs.riderapp.R;
import edu.uga.cs.riderapp.models.Ride;

/**
 * Fragment to display a list of accepted rides for the currently logged-in user.
 * Uses a RecyclerView to present rides stored in the "accepted_rides" node in Firebase.
 */
public class AcceptedRidesFragment extends Fragment {

    private RecyclerView recyclerView;
    private AcceptedRidesAdapter adapter;
    private List<Ride> acceptedRidesList;
    private DatabaseReference acceptedRidesRef;
    private Handler refreshHandler = new Handler();
    private Runnable refreshRunnable;

    public AcceptedRidesFragment() {
        // Required empty public constructor
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater           LayoutInflater object to inflate views.
     * @param container          Parent view the fragment UI should be attached to.
     * @param savedInstanceState Previous state (if any).
     * @return The root view for the fragment's UI.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_accepted_rides, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_accepted_rides);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        acceptedRidesList = new ArrayList<>();
        adapter = new AcceptedRidesAdapter(acceptedRidesList);
        recyclerView.setAdapter(adapter);

        // Reference to the accepted rides node in Firebase
        acceptedRidesRef = FirebaseDatabase.getInstance().getReference("accepted_rides");
        loadAcceptedRides();

        // Periodically refresh adapter to update time-sensitive buttons
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                refreshHandler.postDelayed(this, 60000);
            }
        };
        refreshHandler.postDelayed(refreshRunnable, 60000);

        return view;
    }

    /**
     * Loads accepted rides from Firebase for the current user and updates the adapter.
     */
    private void loadAcceptedRides() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        acceptedRidesRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                acceptedRidesList.clear();
                for (DataSnapshot rideSnapshot : dataSnapshot.getChildren()) {
                    Ride ride = rideSnapshot.getValue(Ride.class);

                    if (ride != null) {
                        acceptedRidesList.add(ride);
                    }
                }
                adapter.setRides(acceptedRidesList); // Notify the adapter of the data change
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load accepted rides", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (refreshHandler != null && refreshRunnable != null) {
            refreshHandler.removeCallbacks(refreshRunnable);
        }
    }

}