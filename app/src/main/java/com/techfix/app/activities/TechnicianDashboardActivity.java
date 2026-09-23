package com.techfix.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.techfix.app.R;

public class TechnicianDashboardActivity extends AppCompatActivity {

    private Button btnAssignedRepairs;
    private Button btnTechnicianLogout;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_technician_dashboard);

        btnAssignedRepairs = findViewById(R.id.btnAssignedRepairs);
        btnTechnicianLogout = findViewById(R.id.btnTechnicianLogout);

        firebaseAuth = FirebaseAuth.getInstance();

        // VIEW ASSIGNED REPAIRS
        btnAssignedRepairs.setOnClickListener(v -> {

            Toast.makeText(
                    TechnicianDashboardActivity.this,
                    "Assigned Repairs page coming next ......",
                    Toast.LENGTH_SHORT
            ).show();

        });

        // LOGOUT
        btnTechnicianLogout.setOnClickListener(v -> {

            firebaseAuth.signOut();

            Intent intent = new Intent(
                    TechnicianDashboardActivity.this,
                    LoginActivitymain.class
            );
            // comment commit

            startActivity(intent);
            finish();

        });
    }
}