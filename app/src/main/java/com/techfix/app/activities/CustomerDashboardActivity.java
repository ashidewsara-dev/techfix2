package com.techfix.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.techfix.app.R;

public class CustomerDashboardActivity extends AppCompatActivity {

    private Button btnBookRepair;
    private Button btnTrackRepair;
    private Button btnServices;
    private Button btnHistory;
    private Button btnBranches;
    private Button btnProfile;
    private Button btnLogout;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_customer_dashboard);

        btnBookRepair = findViewById(R.id.btnBookRepair);
        btnTrackRepair = findViewById(R.id.btnTrackRepair);
        btnServices = findViewById(R.id.btnServices);
        btnHistory = findViewById(R.id.btnHistory);
        btnBranches = findViewById(R.id.btnBranches);
        btnProfile = findViewById(R.id.btnProfile);
        btnLogout = findViewById(R.id.btnLogout);

        firebaseAuth = FirebaseAuth.getInstance();

        // BOOK REPAIR
        btnBookRepair.setOnClickListener(v -> {
            startActivity(new Intent(
                    CustomerDashboardActivity.this,
                    BookRepairActivity.class
            ));
        });

        // TRACK REPAIR
        btnTrackRepair.setOnClickListener(v -> {
            startActivity(new Intent(
                    CustomerDashboardActivity.this,
                    TrackRepairActivity.class
            ));
        });

        // REPAIR SERVICES
        btnServices.setOnClickListener(v -> {
            startActivity(new Intent(
                    CustomerDashboardActivity.this,
                    ServicesActivity.class
            ));
        });

        // REPAIR HISTORY
        btnHistory.setOnClickListener(v -> {
            startActivity(new Intent(
                    CustomerDashboardActivity.this,
                    RepairHistoryActivity.class
            ));
        });

        // FIND NEAREST BRANCH
        btnBranches.setOnClickListener(v -> {
            startActivity(new Intent(
                    CustomerDashboardActivity.this,
                    BranchesActivity.class
            ));
        });

        // MY PROFILE
        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(
                    CustomerDashboardActivity.this,
                    ProfileActivity.class
            ));
        });

        // LOGOUT
        btnLogout.setOnClickListener(v -> {

            firebaseAuth.signOut();

            Intent intent = new Intent(
                    CustomerDashboardActivity.this,
                    LoginActivitymain.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
        });
    }
}