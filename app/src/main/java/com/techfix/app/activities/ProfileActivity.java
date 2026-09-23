package com.techfix.app.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.techfix.app.R;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtName;
    private TextView txtEmail;
    private TextView txtPhone;
    private TextView txtRole;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtPhone = findViewById(R.id.txtPhone);
        txtRole = findViewById(R.id.txtRole);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        loadProfile();
    }


    private void loadProfile() {

        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        String userId =
                firebaseAuth.getCurrentUser().getUid();

        firestore
                .collection("users")
                .document(userId)
                .get()

                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String name =
                                documentSnapshot.getString("name");

                        String email =
                                documentSnapshot.getString("email");

                        String phone =
                                documentSnapshot.getString("phone");

                        String role =
                                documentSnapshot.getString("role");

                        txtName.setText(
                                "Name: " + name
                        );

                        txtEmail.setText(
                                "Email: " + email
                        );

                        txtPhone.setText(
                                "Phone: " + phone
                        );

                        txtRole.setText(
                                "Role: " + role
                        );
                    }
                })

                .addOnFailureListener(e -> {

                    Toast.makeText(
                            ProfileActivity.this,
                            "Failed to load profile: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                });
    }
}