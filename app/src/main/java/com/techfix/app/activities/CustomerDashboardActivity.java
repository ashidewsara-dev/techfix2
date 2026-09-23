package com.techfix.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

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

        // Connect buttons
        btnBookRepair = findViewById(R.id.btnBookRepair);
        btnTrackRepair = findViewById(R.id.btnTrackRepair);
        btnServices = findViewById(R.id.btnServices);
        btnHistory = findViewById(R.id.btnHistory);
        btnBranches = findViewById(R.id.btnBranches);
        btnProfile = findViewById(R.id.btnProfile);
        btnLogout = findViewById(R.id.btnLogout);

        firebaseAuth = FirebaseAuth.getInstance();


        // BOOK A REPAIR
        btnBookRepair.setOnClickListener(v -> {

            Intent intent = new Intent(
                    CustomerDashboardActivity.this,
                    BookRepairActivity.class
            );

            startActivity(intent);
        });


        // TRACK REPAIR
        btnTrackRepair.setOnClickListener(v -> {

            Intent intent = new Intent(
                    CustomerDashboardActivity.this,
                    TrackRepairActivity.class
            );

            startActivity(intent);
        });


        // REPAIR SERVICES
        btnServices.setOnClickListener(v -> {

            Intent intent = new Intent(
                    CustomerDashboardActivity.this,
                    ServiceActivity.class
            );

            startActivity(intent);
        });


        // REPAIR HISTORY
        btnHistory.setOnClickListener(v -> {

            Intent intent = new Intent(
                    CustomerDashboardActivity.this,
                    RepairHistoryActivity.class
            );

            startActivity(intent);
        });


        // FIND A BRANCH
        btnBranches.setOnClickListener(v -> {

            Intent intent = new Intent(
                    CustomerDashboardActivity.this,
                    BranchesActivity.class
            );

            startActivity(intent);
        });


        // MY PROFILE
        btnProfile.setOnClickListener(v -> {

            Intent intent = new Intent(
                    CustomerDashboardActivity.this,
                    ProfileActivity.class
            );

            startActivity(intent);

        });

        // LOGOUT
        btnLogout.setOnClickListener(v -> {

            firebaseAuth.signOut();

            Intent intent = new Intent(
                    CustomerDashboardActivity.this,
                    LoginActivitymain.class
            );

            startActivity(intent);

            finish();
        });
    }
}