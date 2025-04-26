package edu.uga.cs.riderapp.fragments;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import edu.uga.cs.riderapp.R;
import edu.uga.cs.riderapp.databinding.FragmentProposalBinding;
import edu.uga.cs.riderapp.models.Proposal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ProposalRecyclerViewAdapter extends RecyclerView.Adapter<ProposalRecyclerViewAdapter.ViewHolder> {

    private final List<Proposal> proposals;
    private final OnProposalClickListener listener;

    public interface OnProposalClickListener {
        void onAcceptClick(Proposal proposal);
        void onCancelClick(Proposal proposal, View actionButtonsLayout);
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
            boolean isVisible = holder.actionButtonsLayout.getVisibility() == View.VISIBLE;
            holder.actionButtonsLayout.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        });

        // Handle button clicks
        holder.acceptButton.setOnClickListener(v -> {
            listener.onAcceptClick(proposal);
            holder.actionButtonsLayout.setVisibility(View.GONE);
        });

        holder.cancelButton.setOnClickListener(v ->
                listener.onCancelClick(proposal, holder.actionButtonsLayout));
    }

    @Override
    public int getItemCount() {
        return proposals.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final FragmentProposalBinding binding;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        public final LinearLayout actionButtonsLayout;
        public final Button acceptButton;
        public final Button cancelButton;

        public ViewHolder(FragmentProposalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            actionButtonsLayout = binding.actionButtonsLayout;
            acceptButton = binding.acceptButton;
            cancelButton = binding.cancelButton;
        }

        public void bind(Proposal proposal) {
            String proposalName;
            if (proposal.getDriverName() != null) {
                proposalName = proposal.getDriverName() + "'s Ride";
            } else if (proposal.getRiderName() != null) {
                proposalName = proposal.getRiderName() + "'s Request";
            } else {
                proposalName = "Ride Proposal";
            }
            binding.proposalName.setText(proposalName);

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
            actionButtonsLayout.setVisibility(View.GONE);
        }
    }
}
