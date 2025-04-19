package edu.uga.cs.riderapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

import edu.uga.cs.riderapp.R;

public class LoginFragment extends Fragment {

    private EditText emailEditText, passwordEditText;
    private FirebaseAuth auth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        emailEditText = view.findViewById(R.id.editTextEmail);
        passwordEditText = view.findViewById(R.id.editTextPassword);
        Button loginButton = view.findViewById(R.id.buttonLogin);

        auth = FirebaseAuth.getInstance();

        loginButton.setOnClickListener(v -> loginUser());

        return view;
    }

    private void loginUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Invalid email format");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password required");
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Login successful", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(getActivity(), HomeActivity.class));
                        getActivity().finish();
                    } else {
                        Toast.makeText(getActivity(), "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
