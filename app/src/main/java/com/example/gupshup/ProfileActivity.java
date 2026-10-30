package com.example.gupshup;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 101;

    // UI Elements
    CircleImageView profileImage;
    Button btnChangeImage, btnSave, btnResetPassword, btnLogout;
    EditText etName, etEmail;

    // Firebase Elements
    FirebaseAuth auth;
    DatabaseReference userRef;
    StorageReference storageRef;
    String uid;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);

        // 1. Initialize Views from your XML
        profileImage = findViewById(R.id.profileImage);
        btnChangeImage = findViewById(R.id.btnChangeImage);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        btnSave = findViewById(R.id.btnSave);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnLogout = findViewById(R.id.btnLogout);

        // 2. Initialize Firebase
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        uid = user.getUid();
        etEmail.setText(user.getEmail()); // Set email from Auth

        // Database path: Users -> [UID]
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        // Storage path: ProfilePictures folder
        storageRef = FirebaseStorage.getInstance().getReference("ProfilePictures");

        // 3. Load Existing Profile Data (Name and Image)
        loadProfileData();

        // 4. Click Listeners
        btnChangeImage.setOnClickListener(v -> openGallery());

        btnSave.setOnClickListener(v -> saveProfileName());

        btnResetPassword.setOnClickListener(v -> resetPassword(user.getEmail()));

        btnLogout.setOnClickListener(v -> logout());
    }

    // --- GALLERY UPLOAD LOGIC ---

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            // Show the image locally first for better user experience
            profileImage.setImageURI(imageUri);
            // Trigger the Blaze Storage Upload
            uploadImageToFirebase();
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            // Save as [UID].jpg so each user has one unique file
            StorageReference fileRef = storageRef.child(uid + ".jpg");

            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    // Update the image link in Realtime Database
                    userRef.child("profilePic").setValue(downloadUrl);
                    Toast.makeText(this, "Profile Image Updated!", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    // --- DATA LOADING & MANAGEMENT ---

    private void loadProfileData() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("username").getValue(String.class);
                    String pirkUrl = snapshot.child("profilePic").getValue(String.class);

                    if (name != null) etName.setText(name);

                    // Use Glide to load the profile image from Firebase URL
                    if (pirkUrl != null && !pirkUrl.isEmpty()) {
                        Glide.with(ProfileActivity.this)
                                .load(pirkUrl)
                                .placeholder(R.drawable.ic_profile_placeholder)
                                .into(profileImage);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void saveProfileName() {
        String newName = etName.getText().toString().trim();
        if (!newName.isEmpty()) {
            userRef.child("username").setValue(newName)
                    .addOnSuccessListener(aVoid -> Toast.makeText(this, "Name Saved!", Toast.LENGTH_SHORT).show());
        }
    }

    private void resetPassword(String email) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Password Reset Link Sent!", Toast.LENGTH_SHORT).show());
    }

    private void logout() {
        auth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}