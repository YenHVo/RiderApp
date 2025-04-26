package edu.uga.cs.riderapp.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import edu.uga.cs.riderapp.R;
import edu.uga.cs.riderapp.models.RideHistory;

import java.util.List;

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

        holder.startLocation.setText("From: " + ride.getStartLocation());
        holder.endLocation.setText("To: " + ride.getEndLocation());
        holder.date.setText(ride.getDate());
        holder.role.setText(ride.getRole().equals("driver") ? "Driver" : "Rider");
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
