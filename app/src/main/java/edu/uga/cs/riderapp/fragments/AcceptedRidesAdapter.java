package edu.uga.cs.riderapp.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

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

        /**
         * Constructor binds the views from the ride_item layout.
         */
        public RideViewHolder(View itemView) {
            super(itemView);
            startLocationTextView = itemView.findViewById(R.id.start_location);
            endLocationTextView = itemView.findViewById(R.id.end_location);
            dateTimeTextView = itemView.findViewById(R.id.date_time);
            pointsTextView = itemView.findViewById(R.id.points);
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
