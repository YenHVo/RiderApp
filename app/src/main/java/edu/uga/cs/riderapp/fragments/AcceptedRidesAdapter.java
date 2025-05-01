package edu.uga.cs.riderapp.fragments;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import edu.uga.cs.riderapp.activities.LoadingActivity;
import edu.uga.cs.riderapp.models.Ride;
import edu.uga.cs.riderapp.R;

/**
 * Adapter class for displaying a list of accepted rides in a RecyclerView.
 * Each item shows the start and end locations, date/time, and points for the ride.
 */
public class AcceptedRidesAdapter extends RecyclerView.Adapter<AcceptedRidesAdapter.RideViewHolder> {

    private List<Ride> rides;

    /**
     * Constructor to initialize the adapter with a list of rides.
     * @param rides List of Ride objects.
     */
    public AcceptedRidesAdapter(List<Ride> rides) {
        this.rides = rides;
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type.
     * Inflates the ride_item layout.
     */
    @Override
    public RideViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ride_item, parent, false);
        return new RideViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * Binds data from a Ride object to the ViewHolder views.
     */
    @Override
    public void onBindViewHolder(RideViewHolder holder, int position) {
        Ride ride = rides.get(position);

        // Set start and end location text
        holder.startLocationTextView.setText(ride.getStartLocation());
        holder.endLocationTextView.setText(ride.getEndLocation());

        // Format the dateTime as a readable string
        String formattedDate = formatDate(ride.getDateTime());
        holder.dateTimeTextView.setText(formattedDate);

        Long points = ride.getPoints();
        if (points == null) {
            holder.pointsTextView.setText("Points: 0");
        } else {
            holder.pointsTextView.setText("Points: " + points);
        }

        boolean isDriver = ride.getPoints() != null && ride.getPoints() > 0;
        String proposalId = ride.getProposalId();
        DatabaseReference proposalRef = FirebaseDatabase.getInstance().getReference("proposals").child(proposalId);
        String statusField = isDriver ? "driverStatus" : "riderStatus";

        // Listen for status updates
        proposalRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String driverStatus = snapshot.child("driverStatus").getValue(String.class);
                String riderStatus = snapshot.child("riderStatus").getValue(String.class);

                boolean isCancelled = "cancelled".equals(driverStatus) || "cancelled".equals(riderStatus);

                if (isCancelled) {
                    holder.startLocationTextView.setText("Your ride to " + ride.getEndLocation() + " has been cancelled.");
                    holder.endLocationTextView.setText("Please create a new one and try again.");
                    holder.pointsTextView.setVisibility(View.GONE);
                    holder.startRideBtn.setVisibility(View.GONE);
                } else {
                    if (ride.getDateTime() != null && System.currentTimeMillis() >= ride.getDateTime()) {
                        holder.startRideBtn.setVisibility(View.VISIBLE);
                        holder.startRideBtn.setOnClickListener(v -> {
                            proposalRef.child(statusField).setValue("accepted")
                                    .addOnSuccessListener(aVoid -> {
                                        Intent intent = new Intent(v.getContext(), LoadingActivity.class);
                                        intent.putExtra("proposalId", proposalId);
                                        intent.putExtra("isDriver", isDriver);
                                        v.getContext().startActivity(intent);
                                    });
                        });
                    } else {
                        holder.startRideBtn.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(holder.itemView.getContext(), "Failed to load ride status: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Cancel Button behavior
        holder.cancelRideBtn.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();

            if (currentPos == RecyclerView.NO_POSITION || currentPos >= rides.size()) {
                Toast.makeText(v.getContext(), "Ride not found.", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(v.getContext(), "User not signed in.", Toast.LENGTH_SHORT).show();
                return;
            }

            proposalRef.child(statusField).setValue("cancelled")
                    .addOnSuccessListener(aVoid -> {
                        DatabaseReference acceptedRideRef = FirebaseDatabase.getInstance()
                                .getReference("accepted_rides")
                                .child(user.getUid())
                                .child(proposalId);

                        acceptedRideRef.removeValue()
                                .addOnSuccessListener(unused -> {
                                    rides.remove(currentPos);
                                    notifyItemRemoved(currentPos);
                                    notifyItemRangeChanged(currentPos, rides.size());

                                    Toast.makeText(v.getContext(), "Ride has been cancelled and removed.", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(v.getContext(), "Failed to remove ride: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(v.getContext(), "Failed to cancel proposal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });


        /*
        holder.cancelRideBtn.setOnClickListener(v -> {
            // Set current user's status to "cancelled"
            proposalRef.child(statusField).setValue("cancelled")
                    .addOnSuccessListener(aVoid -> {
                        // Remove from list and notify UI
                        rides.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, rides.size());

                        // Toast cancellation
                        Toast.makeText(v.getContext(), "Ride at " + formattedDate + " has been cancelled.", Toast.LENGTH_SHORT).show();
                    });
        });*/
    }

    /**
     * Returns the number of rides in the dataset.
     */
    @Override
    public int getItemCount() {
        return rides.size();
    }

    /**
     * ViewHolder class to cache views for each ride item.
     */
    public static class RideViewHolder extends RecyclerView.ViewHolder {
        TextView startLocationTextView;
        TextView endLocationTextView;
        TextView dateTimeTextView;
        TextView pointsTextView;
        Button startRideBtn;
        ImageButton cancelRideBtn;

        /**
         * Constructor binds the views from the ride_item layout.
         */
        public RideViewHolder(View itemView) {
            super(itemView);
            startLocationTextView = itemView.findViewById(R.id.start_location);
            endLocationTextView = itemView.findViewById(R.id.end_location);
            dateTimeTextView = itemView.findViewById(R.id.date_time);
            pointsTextView = itemView.findViewById(R.id.points);
            startRideBtn = itemView.findViewById(R.id.startRideBtn);
            cancelRideBtn = itemView.findViewById(R.id.cancelRideBtn);
        }
    }

    /**
     * Utility method to format a Long timestamp into a readable date string.
     * @param dateTime The ride's timestamp.
     * @return A formatted date/time string or "Unknown" if null.
     */
    private String formatDate(Long dateTime) {
        if (dateTime == null) {
            return "Unknown";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return sdf.format(dateTime);
    }

    /**
     * Updates the list of rides and refreshes the adapter.
     * @param rides The new list of Ride objects.
     */
    public void setRides(List<Ride> rides) {
        this.rides = rides;
        notifyDataSetChanged();
    }
}
