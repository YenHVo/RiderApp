package edu.uga.cs.riderapp.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

import edu.uga.cs.riderapp.R;
import edu.uga.cs.riderapp.models.User;

/**
 * Fragment that handles user registration.
 * Collects email, password, and name, then registers a user using Firebase Authentication
 * and stores additional user data in the Firebase Realtime Database.
 */
public class SignUpFragment extends Fragment {

    private EditText emailEditText, passwordEditText, nameEditText;
    private Button signUpButton;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;

    public SignUpFragment() {
        // Required empty public constructor
    }

    /**
     * Inflates the fragment layout and sets up listeners and Firebase references.
     *
     * @param inflater LayoutInflater to inflate the fragment layout.
     * @param container ViewGroup that hosts the fragment UI.
     * @param savedInstanceState Previous state if re-created.
     * @return View instance for this fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        // Initialize views
        emailEditText = view.findViewById(R.id.email_edit_text);
        passwordEditText = view.findViewById(R.id.password_edit_text);
        nameEditText = view.findViewById(R.id.name_edit_text);
        signUpButton = view.findViewById(R.id.sign_up_btn);
        progressBar = view.findViewById(R.id.progressBar);

        // Initialize Firebase authentication and database
        mAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("users");

        // Set listener to sign up button
        signUpButton.setOnClickListener(v -> registerUser());

        return view;
    }

    /**
     * Handles user input validation, Firebase Authentication registration,
     * and saving user details in the Realtime Database.
     */
    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();

        // Validate email format
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Invalid email format");
            emailEditText.requestFocus();
            return;
        }

        // Validate password length
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            passwordEditText.requestFocus();
            return;
        }

        // Ensure name is provided
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Name is required");
            nameEditText.requestFocus();
            return;
        }

        // Show loading indicator
        progressBar.setVisibility(View.VISIBLE);

        // Create user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            User newUser = new User(userId, email, name, 0, new Date());

                            // Save user data in Firebase Realtime Database
                            databaseRef.child(userId).setValue(newUser)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(getContext(), "Registration successful!", Toast.LENGTH_SHORT).show();

                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Failed to save user: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        }
                    } else {
                        Toast.makeText(getContext(), "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
