
package com.techfix.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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
    private TextView txtProfileInitial;
    private TextView txtProfileSubtitle;

    private ProgressBar profileProgressBar;
    private Button btnRefreshProfile;

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

        txtProfileInitial = findViewById(R.id.txtProfileInitial);
        txtProfileSubtitle = findViewById(R.id.txtProfileSubtitle);

        profileProgressBar = findViewById(R.id.profileProgressBar);
        btnRefreshProfile = findViewById(R.id.btnRefreshProfile);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        btnRefreshProfile.setOnClickListener(v -> loadProfile());

        loadProfile();
    }

    private void loadProfile() {

        if (firebaseAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please log in again",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        String userId = firebaseAuth.getCurrentUser().getUid();

        profileProgressBar.setVisibility(View.VISIBLE);
        btnRefreshProfile.setEnabled(false);

        firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    profileProgressBar.setVisibility(View.GONE);
                    btnRefreshProfile.setEnabled(true);

                    if (!documentSnapshot.exists()) {

                        txtProfileInitial.setText("?");
                        txtProfileSubtitle.setText(
                                "Profile details unavailable"
                        );

                        txtName.setText("Not available");
                        txtPhone.setText("Not available");
                        txtRole.setText("Not available");

                        String accountEmail =
                                firebaseAuth.getCurrentUser() != null
                                        ? firebaseAuth.getCurrentUser().getEmail()
                                        : null;

                        txtEmail.setText(safeValue(accountEmail));

                        Toast.makeText(
                                this,
                                "No profile document found for this account",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    String name = documentSnapshot.getString("name");
                    String email = documentSnapshot.getString("email");
                    String phone = documentSnapshot.getString("phone");
                    String role = documentSnapshot.getString("role");

                    txtName.setText(safeValue(name));
                    txtPhone.setText(safeValue(phone));
                    txtRole.setText(safeValue(role));

                    // If Firestore has no email, show the
                    // signed-in account's email instead.
                    if (email == null || email.trim().isEmpty()) {

                        email = firebaseAuth.getCurrentUser() != null
                                ? firebaseAuth.getCurrentUser().getEmail()
                                : null;
                    }

                    txtEmail.setText(safeValue(email));

                    String displayName = safeValue(name);

                    txtProfileSubtitle.setText(displayName);

                    if (!displayName.equals("Not available")) {

                        txtProfileInitial.setText(
                                displayName.substring(0, 1).toUpperCase()
                        );

                    } else {

                        txtProfileInitial.setText("?");
                    }
                })
                .addOnFailureListener(e -> {

                    profileProgressBar.setVisibility(View.GONE);
                    btnRefreshProfile.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Failed to load profile: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private String safeValue(String value) {

        if (value == null || value.trim().isEmpty()) {
            return "Not available";
        }

        return value;
    }
}
