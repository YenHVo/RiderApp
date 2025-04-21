package edu.uga.cs.riderapp.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import edu.uga.cs.riderapp.R;
import edu.uga.cs.riderapp.fragments.placeholder.PlaceholderContent;
import edu.uga.cs.riderapp.models.Proposal;
import edu.uga.cs.riderapp.models.User;

/**
 * A fragment representing a list of Items.
 */
public class ProposalListFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProposalRecyclerViewAdapter adapter;

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProposalListFragment() {
    }
    @SuppressWarnings("unused")
    public static ProposalListFragment newInstance(int columnCount) {
        ProposalListFragment fragment = new ProposalListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_proposal_list, container, false);

        // Set up the RecyclerView
        recyclerView = view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // sample data, will delete later
        List<Proposal> proposals = getProposals();
        adapter = new ProposalRecyclerViewAdapter(proposals, proposal -> {
            // todo: Handle item click
            //showProposalDetails(proposal);
        });

        recyclerView.setAdapter(adapter);

        return view;
    }

    // todo: use this class to extract proposals from the firebase auth and create Proposal Objects with it
    private List<Proposal> getProposals() {
        List<Proposal> proposals = new ArrayList<>();

        // Sample driver proposal (ride offer)
        User driverJohn = new User("john@example.com", "John");
        driverJohn.setName("John D.");
        proposals.add(new Proposal(
                "offer",
                "Downtown Athens",
                "UGA Main Campus",
                driverJohn,
                "Toyota Camry",
                3
        ));

        // Sample driver proposal (ride offer)
        User driverSarah = new User("sarah@example.com", "Sarah");
        driverSarah.setName("Sarah M.");
        proposals.add(new Proposal(
                "offer",
                "Atlanta Airport",
                "UGA Campus",
                driverSarah,
                "Honda Accord",
                2
        ));

        // Sample rider proposal (ride request)
        User riderMike = new User("mike@example.com", "Mike");
        riderMike.setName("Mike T.");
        proposals.add(new Proposal(
                "request",
                "UGA Science Library",
                "Athens Mall",
                riderMike
        ));

        // Sample rider proposal (ride request)
        User riderEmma = new User("emma@example.com", "Emma");
        riderEmma.setName("Emma G.");
        proposals.add(new Proposal(
                "request",
                "East Campus Village",
                "Downtown Athens",
                riderEmma
        ));

        return proposals;
    }
}