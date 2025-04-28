package edu.uga.cs.riderapp.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import edu.uga.cs.riderapp.R;
import edu.uga.cs.riderapp.models.RideHistory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RideHistoryAdapter extends RecyclerView.Adapter<RideHistoryAdapter.ViewHolder> {

    private List<RideHistory> rideList;

    public RideHistoryAdapter(List<RideHistory> rideList) {
        this.rideList = rideList;
    }

    @Override
    public RideHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ride_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RideHistoryAdapter.ViewHolder holder, int position) {
        RideHistory ride = rideList.get(position);

        // Safely set the text for startLocation, endLocation, and date
        holder.startLocation.setText(ride.getStartLocation() != null ? "From: " + ride.getStartLocation() : "From: N/A");
        holder.endLocation.setText(ride.getEndLocation() != null ? "To: " + ride.getEndLocation() : "To: N/A");

        // Handle date formatting if it's a long (Unix timestamp)
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String formattedDate = ride.getDate() != null ? sdf.format(new Date(ride.getDate())) : "Date: N/A";
        holder.date.setText(formattedDate);

        // Handle the role value safely
        String roleText = "Unknown";
        if ("driver".equalsIgnoreCase(ride.getRole())) {
            roleText = "Driver";
        } else if ("rider".equalsIgnoreCase(ride.getRole())) {
            roleText = "Rider";
        }
        holder.role.setText(roleText);
    }

    @Override
    public int getItemCount() {
        return rideList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView startLocation, endLocation, date, role;

        public ViewHolder(View itemView) {
            super(itemView);
            startLocation = itemView.findViewById(R.id.textViewStartLocation);
            endLocation = itemView.findViewById(R.id.textViewEndLocation);
            date = itemView.findViewById(R.id.textViewDate);
            role = itemView.findViewById(R.id.textViewRole);
        }
    }
}
