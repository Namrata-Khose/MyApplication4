package com.example.gupshup;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    EditText etName, etEmail, etPassword;
    Button btnSignup;
    TextView tvLogin;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        // Ensure these IDs match your activity_signup.xml
        etName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignup = findViewById(R.id.btnSignup);
        tvLogin = findViewById(R.id.tvLogin);

        btnSignup.setOnClickListener(v -> registerUser());

        // Redirecting back to Login instead of Main for better flow
        tvLogin.setOnClickListener(v -> {
            finish(); // Simply closes this activity to return to LoginActivity
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                    if (firebaseUser == null) return;

                    String uid = firebaseUser.getUid();

                    // Saving user data to Realtime Database
                    User user = new User(uid, name, email);

                    FirebaseDatabase.getInstance()
                            .getReference("Users")
                            .child(uid)
                            .setValue(user)
                            .addOnSuccessListener(unused -> {
                                // Initialize presence status to match ChatActivity logic
                                FirebaseDatabase.getInstance().getReference("State")
                                        .child(uid).child("status").setValue("online");
                                FirebaseDatabase.getInstance().getReference("State")
                                        .child(uid).child("typingId").setValue("none");

                                Toast.makeText(this, "Signup Successful", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            });
                });
    }
}