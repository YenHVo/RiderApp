package edu.uga.cs.riderapp.fragments;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import edu.uga.cs.riderapp.databinding.FragmentProposalBinding;
import edu.uga.cs.riderapp.models.Proposal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Adapter class for displaying a list of ride proposals in a RecyclerView.
 * Handles UI binding, visibility toggling, and interaction callbacks for accepting or canceling proposals.
 */
public class ProposalRecyclerViewAdapter extends RecyclerView.Adapter<ProposalRecyclerViewAdapter.ViewHolder> {

    private final List<Proposal> proposals;
    private final OnProposalClickListener listener;

    /**
     * Interface for handling user actions on proposals (Accept/Cancel).
     */
    public interface OnProposalClickListener {
        void onAcceptClick(Proposal proposal);
        void onCancelClick(Proposal proposal, View actionButtonsLayout);
    }

    /**
     * Constructs the adapter with a list of proposals and a listener.
     *
     * @param items    List of Proposal objects to display.
     * @param listener Listener to handle click events.
     */
    public ProposalRecyclerViewAdapter(List<Proposal> items, OnProposalClickListener listener) {
        this.proposals = items;
        this.listener = listener;
    }

    /**
     * Inflates the layout for each item in the RecyclerView.
     *
     * @param parent   The parent ViewGroup.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder.
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FragmentProposalBinding binding = FragmentProposalBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    /**
     * Binds data to the ViewHolder for each item.
     *
     * @param holder   The ViewHolder to bind data to.
     * @param position The position of the item in the list.
     */
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Proposal proposal = proposals.get(position);
        holder.bind(proposal);

        // Toggle visibility of action buttons and car input based on proposal type
        holder.itemView.setOnClickListener(v -> {
            boolean isVisible = holder.actionButtonsLayout.getVisibility() == View.VISIBLE;
            holder.actionButtonsLayout.setVisibility(isVisible ? View.GONE : View.VISIBLE);

            // Show car details only for requests
            if ("request".equals(proposal.getType())) {
                holder.carDetailsContainer.setVisibility(isVisible ? View.GONE : View.VISIBLE);
            } else {
                holder.carDetailsContainer.setVisibility(View.GONE);
            }
        });

        // Handle accept button click
        holder.acceptButton.setOnClickListener(v -> {
            // Get car details if this is a request
            if ("request".equals(proposal.getType())) {
                String carModel = holder.carModelInput.getText().toString();
                String seatsStr = holder.carSeatsInput.getText().toString();

                if (carModel.isEmpty() || seatsStr.isEmpty()) {
                    Toast.makeText(holder.itemView.getContext(),
                            "Please enter car details", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Store these values in the proposal if needed
                proposal.setCar(carModel);
                try {
                    proposal.setAvailableSeats(Integer.parseInt(seatsStr));
                } catch (NumberFormatException e) {
                    proposal.setAvailableSeats(0);
                }
            }

            listener.onAcceptClick(proposal);
            holder.actionButtonsLayout.setVisibility(View.GONE);
            holder.carDetailsContainer.setVisibility(View.GONE);
        });

        holder.cancelButton.setOnClickListener(v -> {
                listener.onCancelClick(proposal, holder.actionButtonsLayout);
                holder.carDetailsContainer.setVisibility(View.GONE);
        });
    }

    /**
     * Returns the total number of items in the dataset.
     *
     * @return The item count.
     */
    @Override
    public int getItemCount() {
        return proposals.size();
    }

    /**
     * ViewHolder class that holds and binds all views for a single proposal item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final FragmentProposalBinding binding;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        public final LinearLayout actionButtonsLayout;
        public final Button acceptButton;
        public final Button cancelButton;
        public final LinearLayout carDetailsContainer;
        public final EditText carModelInput;
        public final EditText carSeatsInput;

        /**
         * Constructs the ViewHolder and initializes the view references from binding.
         *
         * @param binding The binding object associated with the layout.
         */
        public ViewHolder(FragmentProposalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            actionButtonsLayout = binding.actionButtonsLayout;
            acceptButton = binding.acceptButton;
            cancelButton = binding.cancelButton;
            carDetailsContainer = binding.carDetailsContainer;
            carModelInput = binding.carModelInput;
            carSeatsInput = binding.carSeatsInput;
        }

        /**
         * Binds the Proposal data to the UI components.
         *
         * @param proposal The Proposal object containing data to display.
         */
        public void bind(Proposal proposal) {
            // Set the title using the name of the driver or rider
            String proposalName;
            if (proposal.getDriverName() != null) {
                proposalName = proposal.getDriverName() + "'s Ride";
            } else if (proposal.getRiderName() != null) {
                proposalName = proposal.getRiderName() + "'s Request";
            } else {
                proposalName = "Ride Proposal";
            }
            binding.proposalName.setText(proposalName);

            // Capitalize type string and set it
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
