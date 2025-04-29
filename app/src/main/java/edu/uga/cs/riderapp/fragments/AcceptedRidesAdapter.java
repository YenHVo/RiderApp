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

public class AcceptedRidesAdapter extends RecyclerView.Adapter<AcceptedRidesAdapter.RideViewHolder> {

    private List<Ride> rides;

    public AcceptedRidesAdapter(List<Ride> rides) {
        this.rides = rides;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RideViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ride_item, parent, false);
        return new RideViewHolder(view);
    }

    /*
    @Override
    public void onBindViewHolder(RideViewHolder holder, int position) {
        Ride ride = rides.get(position);
        holder.startLocationTextView.setText(ride.getStartLocation());
        holder.endLocationTextView.setText(ride.getEndLocation());

        // Format the dateTime as a readable string
        String formattedDate = formatDate(ride.getDateTime());
        holder.dateTimeTextView.setText(formattedDate);

        holder.pointsTextView.setText("Points: " + ride.getPoints());
    }*/

    @Override
    public void onBindViewHolder(RideViewHolder holder, int position) {
        Ride ride = rides.get(position);
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

    // Return the size of the dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return rides.size();
    }

    // ViewHolder class to hold the ride item views
    public static class RideViewHolder extends RecyclerView.ViewHolder {
        TextView startLocationTextView;
        TextView endLocationTextView;
        TextView dateTimeTextView;
        TextView pointsTextView;

        public RideViewHolder(View itemView) {
            super(itemView);
            startLocationTextView = itemView.findViewById(R.id.start_location);
            endLocationTextView = itemView.findViewById(R.id.end_location);
            dateTimeTextView = itemView.findViewById(R.id.date_time);
            pointsTextView = itemView.findViewById(R.id.points);
        }
    }

    // Method to format dateTime into a readable format
    private String formatDate(Long dateTime) {
        if (dateTime == null) {
            return "Unknown";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return sdf.format(dateTime);
    }

    // Method to update the data in the adapter
    public void setRides(List<Ride> rides) {
        this.rides = rides;
        notifyDataSetChanged();
    }
}
