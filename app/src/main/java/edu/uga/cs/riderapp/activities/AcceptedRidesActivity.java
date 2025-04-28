package edu.uga.cs.riderapp.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


import edu.uga.cs.riderapp.R;
import edu.uga.cs.riderapp.fragments.AcceptedRidesAdapter;
import edu.uga.cs.riderapp.models.Ride;


public class AcceptedRidesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AcceptedRidesAdapter adapter;
    private List<Ride> acceptedRidesList;

    private DatabaseReference acceptedRidesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accepted_rides);

        recyclerView = findViewById(R.id.recycler_view_accepted_rides);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        acceptedRidesList = new ArrayList<>();
        adapter = new AcceptedRidesAdapter(acceptedRidesList);
        recyclerView.setAdapter(adapter);

        acceptedRidesRef = FirebaseDatabase.getInstance().getReference("accepted_rides");

        loadAcceptedRides();
    }

    private void loadAcceptedRides() {
        // Query Firebase for accepted rides
        acceptedRidesRef.orderByChild("dateTime").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                acceptedRidesList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Ride ride = snapshot.getValue(Ride.class);
                    if (ride != null) {
                        acceptedRidesList.add(ride);
                    }
                }
                adapter.setRides(acceptedRidesList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AcceptedRidesActivity.this, "Failed to load accepted rides", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
