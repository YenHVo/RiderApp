package edu.uga.cs.riderapp.fragments;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.uga.cs.riderapp.databinding.FragmentProposalBinding;
import edu.uga.cs.riderapp.models.Proposal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ProposalRecyclerViewAdapter extends RecyclerView.Adapter<ProposalRecyclerViewAdapter.ViewHolder> {

    private final List<Proposal> proposals;
    private final OnProposalClickListener listener;

    public interface OnProposalClickListener {
        void onProposalClick(Proposal proposal);
    }

    public ProposalRecyclerViewAdapter(List<Proposal> items, OnProposalClickListener listener) {
        this.proposals = items;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FragmentProposalBinding binding = FragmentProposalBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Proposal proposal = proposals.get(position);
        holder.bind(proposal);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProposalClick(proposal);
            }
        });
    }

    @Override
    public int getItemCount() {
        return proposals.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final FragmentProposalBinding binding;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        public ViewHolder(FragmentProposalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Proposal proposal) {
            // Set proposal name based on type and user
            String proposalName;
            if (proposal.getDriver() != null) {
                proposalName = proposal.getDriver().getName() + "'s Ride";
            } else if (proposal.getRider() != null) {
                proposalName = proposal.getRider().getName() + "'s Request";
            } else {
                proposalName = "Ride Proposal";
            }
            binding.proposalName.setText(proposalName);

            // Set type (capitalize first letter)
            String type = proposal.getType();
            if (type != null && !type.isEmpty()) {
                type = type.substring(0, 1).toUpperCase() + type.substring(1);
            }
            binding.proposalType.setText(type);

            // Format date
            String formattedDate = dateFormat.format(proposal.getCreatedAt());
            binding.proposalDate.setText(formattedDate);

            // Set locations
            binding.proposalStartLocation.setText(proposal.getStartLocation());
            binding.proposalDestination.setText(proposal.getEndLocation());

            // Set details
            String details = "";
            if (proposal.getType().equals("offer")) {
                details = String.format("%s • %d seat%s available",
                        proposal.getCar(),
                        proposal.getAvailableSeats(),
                        proposal.getAvailableSeats() != 1 ? "s" : "");
            } else {
                details = "Looking for a ride";
            }
            binding.proposalDetails.setText(details);
        }
    }
}
