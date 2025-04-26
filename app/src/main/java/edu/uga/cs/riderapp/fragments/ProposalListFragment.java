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
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    private List<Proposal> proposals = new ArrayList<>();


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

        recyclerView = view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ProposalRecyclerViewAdapter(proposals, proposal -> {
            // todo: Handle item click
        });

        recyclerView.setAdapter(adapter);

        loadProposalsFromFirebase();

        return view;
    }

    private void loadProposalsFromFirebase() {
        DatabaseReference proposalsRef = FirebaseDatabase.getInstance().getReference("proposals");

        proposalsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                proposals.clear();

                for (DataSnapshot proposalSnap : snapshot.getChildren()) {
                    String proposalId = proposalSnap.getKey();
                    String type = proposalSnap.child("type").getValue(String.class);
                    String startLocation = proposalSnap.child("startLocation").getValue(String.class);
                    String endLocation = proposalSnap.child("endLocation").getValue(String.class);
                    String userId = proposalSnap.child("userId").getValue(String.class);

                    Proposal proposal = new Proposal();
                    proposal.setProposalId(proposalId);
                    proposal.setType(type);
                    proposal.setStartLocation(startLocation);
                    proposal.setEndLocation(endLocation);


                    if ("offer".equals(type)) {
                        String carModel = proposalSnap.child("carModel").getValue(String.class);
                        Integer seats = proposalSnap.child("seatsAvailable").getValue(Integer.class);
                        proposal.setCar(carModel);
                        proposal.setAvailableSeats(seats != null ? seats : 0);
                    }

                    fetchUserById(userId, proposal);

                    proposals.add(proposal);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load proposals.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserById(String userId, Proposal proposal) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                if ("offer".equals(proposal.getType())) {
                    proposal.setDriverId(userId);
                } else {
                    proposal.setRiderId(userId);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getContext(), "Failed to userID.", Toast.LENGTH_SHORT).show();

            }
        });
    }


}
    // todo: use this class to extract proposals from the firebase auth and create Proposal Objects with it
   // private List<Proposal> getProposals() {
    //    List<Proposal> proposals = new ArrayList<>();

        // Sample driver proposal (ride offer)
      //  User driverJohn = new User("john@example.com", "John");
      //  driverJohn.setName("John D.");
       // proposals.add(new Proposal(
        //        "offer",
        //        "Downtown Athens",
        //        "UGA Main Campus",
       //         driverJohn,
       //         "Toyota Camry",
       //         3
      //  ));

        // Sample driver proposal (ride offer)
      //  User driverSarah = new User("sarah@example.com", "Sarah");
        //driverSarah.setName("Sarah M.");
        //proposals.add(new Proposal(
          //      "offer",
            //    "Atlanta Airport",
              //  "UGA Campus",
                //driverSarah,
                //"Honda Accord",
                //2
        //));

        // Sample rider proposal (ride request)
       // User riderMike = new User("mike@example.com", "Mike");
        //riderMike.setName("Mike T.");
      //  proposals.add(new Proposal(
       //         "request",
      //          "UGA Science Library",
       //         "Athens Mall",
      //          riderMike
  //      ));

        // Sample rider proposal (ride request)
     //   User riderEmma = new User("emma@example.com", "Emma");
     //   riderEmma.setName("Emma G.");
     //   proposals.add(new Proposal(
     //           "request",
      //          "East Campus Village",
       //         "Downtown Athens",
        //        riderEmma
       // ));

       // return proposals;
   // }

