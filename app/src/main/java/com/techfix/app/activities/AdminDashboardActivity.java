package com.techfix.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.techfix.app.R;

public class AdminDashboardActivity extends AppCompatActivity {

    private Button btnManageAppointments;
    private Button btnManageServices;
    private Button btnManageTechnicians;
    private Button btnManageParts;
    private Button btnAdminLogout;

    private TextView txtTotalRepairs;
    private TextView txtPendingRepairs;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_admin_dashboard);

        // CONNECT XML ELEMENTS

        btnManageAppointments =
                findViewById(R.id.btnManageAppointments);

        btnManageServices =
                findViewById(R.id.btnManageServices);

        btnManageTechnicians =
                findViewById(R.id.btnManageTechnicians);

        btnManageParts =
                findViewById(R.id.btnManageParts);

        btnAdminLogout =
                findViewById(R.id.btnAdminLogout);

        txtTotalRepairs =
                findViewById(R.id.txtTotalRepairs);

        txtPendingRepairs =
                findViewById(R.id.txtPendingRepairs);

        // FIREBASE

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();


        // MANAGE REPAIR APPOINTMENTS

        btnManageAppointments.setOnClickListener(v -> {

            Intent intent = new Intent(
                    AdminDashboardActivity.this,
                    ManageAppointmentsActivity.class
            );

            startActivity(intent);
        });


        // MANAGE SERVICES

        btnManageServices.setOnClickListener(v -> {

            Intent intent = new Intent(
                    AdminDashboardActivity.this,
                    ManageServicesActivity.class
            );

            startActivity(intent);
        });

        // MANAGE TECHNICIANS

        btnManageTechnicians.setOnClickListener(v -> {

            Toast.makeText(
                    AdminDashboardActivity.this,
                    "Technician management coming next",
                    Toast.LENGTH_SHORT
            ).show();
        });


        // MANAGE SPARE PARTS

        btnManageParts.setOnClickListener(v -> {

            Toast.makeText(
                    AdminDashboardActivity.this,
                    "Spare parts management coming next",
                    Toast.LENGTH_SHORT
            ).show();
        });


        // LOGOUT

        btnAdminLogout.setOnClickListener(v -> {

            firebaseAuth.signOut();

            Intent intent = new Intent(
                    AdminDashboardActivity.this,
                    LoginActivitymain.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
        });
    }


    // REFRESH STATISTICS WHEN DASHBOARD OPENS

    @Override
    protected void onStart() {
        super.onStart();

        loadDashboardStatistics();
    }


    // LOAD REPAIR STATISTICS FROM FIREBASE

    private void loadDashboardStatistics() {

        firestore.collection("appointments")
                .get()

                .addOnSuccessListener(queryDocumentSnapshots -> {

                    int totalRepairs =
                            queryDocumentSnapshots.size();

                    int pendingRepairs = 0;

                    for (QueryDocumentSnapshot document
                            : queryDocumentSnapshots) {

                        String status =
                                document.getString("status");

                        if ("Pending".equalsIgnoreCase(status)) {
                            pendingRepairs++;
                        }
                    }

                    txtTotalRepairs.setText(
                            String.valueOf(totalRepairs)
                    );

                    txtPendingRepairs.setText(
                            String.valueOf(pendingRepairs)
                    );
                })

                .addOnFailureListener(e -> {

                    txtTotalRepairs.setText("—");
                    txtPendingRepairs.setText("—");

                    Toast.makeText(
                            AdminDashboardActivity.this,
                            "Failed to load statistics: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}