package edu.uga.cs.riderapp.fragments;

import android.os.Bundle;
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

public class AcceptedRidesFragment extends Fragment {

    private RecyclerView recyclerView;
    private AcceptedRidesAdapter adapter;
    private List<Ride> acceptedRidesList;
    private DatabaseReference acceptedRidesRef;

    public AcceptedRidesFragment() {
        // Required empty public constructor
    }

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

        acceptedRidesRef = FirebaseDatabase.getInstance().getReference("accepted_rides");

        loadAcceptedRides();

        return view;
    }

    private void loadAcceptedRides() {
        // Query Firebase for accepted rides by both driverId and riderId
        acceptedRidesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                acceptedRidesList.clear();
                for (DataSnapshot driverSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot rideSnapshot : driverSnapshot.getChildren()) {
                        Ride ride = rideSnapshot.getValue(Ride.class);
                        if (ride != null) {
                            acceptedRidesList.add(ride);
                        }
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
}