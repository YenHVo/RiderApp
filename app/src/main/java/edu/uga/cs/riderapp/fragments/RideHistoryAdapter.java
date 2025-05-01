package edu.uga.cs.riderapp.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import edu.uga.cs.riderapp.R;
import edu.uga.cs.riderapp.models.RideHistory;

import java.util.List;

/**
 * Adapter for displaying ride history in a RecyclerView.
 * Each item shows the ride's start location, end location, date, and user role.
 */
public class RideHistoryAdapter extends RecyclerView.Adapter<RideHistoryAdapter.ViewHolder> {

    private List<RideHistory> rideList;

    /**
     * Constructor to initialize the adapter with a list of rides.
     *
     * @param rideList List of RideHistory objects to display.
     */
    public RideHistoryAdapter(List<RideHistory> rideList) {
        this.rideList = rideList;
    }

    /**
     * Inflates the layout for each ride item in the RecyclerView.
     *
     * @param parent   The parent ViewGroup.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder instance containing the inflated view.
     */
    @Override
    public RideHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ride_history, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Binds data from a RideHistory object to the UI components in the ViewHolder.
     *
     * @param holder   The ViewHolder to bind data to.
     * @param position The position of the ride in the list.
     */
    @Override
    public void onBindViewHolder(RideHistoryAdapter.ViewHolder holder, int position) {
        RideHistory ride = rideList.get(position);

        // Safely set the text for startLocation, endLocation, and date
        holder.startLocation.setText(ride.getStartLocation() != null ? "From: " + ride.getStartLocation() : "From: N/A");
        holder.endLocation.setText(ride.getEndLocation() != null ? "To: " + ride.getEndLocation() : "To: N/A");
        holder.date.setText(ride.getDate() != null ? ride.getDate() : "Date: N/A");

        // Handle the role value safely
        String roleText = "Unknown";
        if ("driver".equalsIgnoreCase(ride.getRole())) {
            roleText = "Driver";
        } else if ("rider".equalsIgnoreCase(ride.getRole())) {
            roleText = "Rider";
        }
        holder.role.setText(roleText);
    }

    /**
     * Returns the total number of rides in the dataset.
     *
     * @return Total item count.
     */
    @Override
    public int getItemCount() {
        return rideList.size();
    }

    /**
     * ViewHolder class that holds the UI elements for a single ride history item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView startLocation, endLocation, date, role;

        /**
         * Constructor that initializes the TextViews for ride details.
         *
         * @param itemView The root view of the item layout.
         */
        public ViewHolder(View itemView) {
            super(itemView);
            startLocation = itemView.findViewById(R.id.textViewStartLocation);
            endLocation = itemView.findViewById(R.id.textViewEndLocation);
            date = itemView.findViewById(R.id.textViewDate);
            role = itemView.findViewById(R.id.textViewRole);
        }
    }
}
