package edu.uga.cs.riderapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.uga.cs.riderapp.R;
import edu.uga.cs.riderapp.activities.HomeActivity;
import edu.uga.cs.riderapp.models.User;

/**
 * Fragment responsible for handling user login functionality.
 * Validates credentials, authenticates with Firebase, and navigates to HomeActivity on success.
 */

public class LoginFragment extends Fragment {

    private EditText emailEditText, passwordEditText;
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Inflates the login layout, initializes FirebaseAuth and sets up login button listener.
     *
     * @param inflater           LayoutInflater to inflate the layout.
     * @param container          Optional parent view.
     * @param savedInstanceState Saved state bundle.
     * @return the root view of the fragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        // Initialize views
        emailEditText = view.findViewById(R.id.editTextEmail);
        passwordEditText = view.findViewById(R.id.editTextPassword);
        Button loginButton = view.findViewById(R.id.buttonLogin);

        // Initialize Firebase authentication and database
        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Set listener to login button
        loginButton.setOnClickListener(v -> loginUser());

        return view;
    }

    /**
     * Handles user login by validating input, signing in with FirebaseAuth,
     * and retrieving user data from Firebase Realtime Database.
     */
    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate email format
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Invalid email format");
            return;
        }

        // Validate password field is not empty
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password required");
            return;
        }

        // Attempt Firebase login
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();

                            databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    User user = snapshot.getValue(User.class);
                                    if (user != null) {
                                        if (getActivity() != null) {
                                            Log.d("LoginFragment", "Activity is not null, proceeding");
                                            Toast.makeText(getActivity(), "Welcome, " + user.getName(), Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(getActivity(), HomeActivity.class));
                                            getActivity().finish();
                                        } else {
                                            Log.e("LoginFragment", "getActivity() is null, cannot transition to HomeActivity");
                                        }
                                    } else {
                                        if (getActivity() != null) {
                                            Toast.makeText(getActivity(), "User record not found in database", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(getActivity(), "Database error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(getActivity(), "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
