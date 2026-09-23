package com.techfix.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.techfix.app.R;

public class ManageAppointmentsActivity extends AppCompatActivity {

    private LinearLayout adminAppointmentsContainer;
    private ProgressBar adminAppointmentsProgressBar;
    private TextView txtNoAppointments;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_manage_appointments);

        adminAppointmentsContainer =
                findViewById(R.id.adminAppointmentsContainer);

        adminAppointmentsProgressBar =
                findViewById(R.id.adminAppointmentsProgressBar);

        txtNoAppointments =
                findViewById(R.id.txtNoAppointments);

        firestore = FirebaseFirestore.getInstance();

        loadAppointments();
    }


    // LOAD ALL REPAIR APPOINTMENTS
    private void loadAppointments() {

        adminAppointmentsProgressBar.setVisibility(View.VISIBLE);

        firestore.collection("appointments")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    adminAppointmentsProgressBar.setVisibility(View.GONE);
                    adminAppointmentsContainer.removeAllViews();

                    if (queryDocumentSnapshots.isEmpty()) {

                        txtNoAppointments.setVisibility(View.VISIBLE);
                        return;
                    }

                    txtNoAppointments.setVisibility(View.GONE);

                    for (QueryDocumentSnapshot document
                            : queryDocumentSnapshots) {

                        String appointmentId = document.getId();

                        String category =
                                document.getString("deviceCategory");

                        String brand =
                                document.getString("brand");

                        String model =
                                document.getString("model");

                        String problem =
                                document.getString("problem");

                        String branch =
                                document.getString("branch");

                        String date =
                                document.getString("preferredDate");

                        String status =
                                document.getString("status");


                        // APPOINTMENT DETAILS
                        TextView appointmentView =
                                new TextView(
                                        ManageAppointmentsActivity.this
                                );

                        String details =
                                "Device: " + brand + " " + model
                                        + "\nCategory: " + category
                                        + "\nProblem: " + problem
                                        + "\nBranch: " + branch
                                        + "\nDate: " + date
                                        + "\nStatus: " + status;

                        appointmentView.setText(details);
                        appointmentView.setTextSize(16);

                        appointmentView.setPadding(
                                30,
                                30,
                                30,
                                15
                        );


                        // SET PENDING BUTTON
                        Button btnPending =
                                new Button(
                                        ManageAppointmentsActivity.this
                                );

                        btnPending.setText("Set Pending");


                        // SET IN PROGRESS BUTTON
                        Button btnInProgress =
                                new Button(
                                        ManageAppointmentsActivity.this
                                );

                        btnInProgress.setText("Set In Progress");


                        // SET COMPLETED BUTTON
                        Button btnCompleted =
                                new Button(
                                        ManageAppointmentsActivity.this
                                );

                        btnCompleted.setText("Set Completed");


                        // ASSIGN TECHNICIAN BUTTON
                        Button btnAssignTechnician =
                                new Button(
                                        ManageAppointmentsActivity.this
                                );

                        btnAssignTechnician.setText("Assign Technician");


                        // PENDING BUTTON ACTION
                        btnPending.setOnClickListener(v -> {

                            updateStatus(
                                    appointmentId,
                                    "Pending"
                            );

                        });


                        // IN PROGRESS BUTTON ACTION
                        btnInProgress.setOnClickListener(v -> {

                            updateStatus(
                                    appointmentId,
                                    "In Progress"
                            );

                        });


                        // COMPLETED BUTTON ACTION
                        btnCompleted.setOnClickListener(v -> {

                            updateStatus(
                                    appointmentId,
                                    "Completed"
                            );

                        });


                        // ASSIGN TECHNICIAN BUTTON ACTION
                        btnAssignTechnician.setOnClickListener(v -> {

                            Toast.makeText(
                                    ManageAppointmentsActivity.this,
                                    "Technician assignment coming next",
                                    Toast.LENGTH_SHORT
                            ).show();

                        });


                        // ADD DETAILS AND BUTTONS TO SCREEN
                        adminAppointmentsContainer
                                .addView(appointmentView);

                        adminAppointmentsContainer
                                .addView(btnPending);

                        adminAppointmentsContainer
                                .addView(btnInProgress);

                        adminAppointmentsContainer
                                .addView(btnCompleted);

                        adminAppointmentsContainer
                                .addView(btnAssignTechnician);
                    }

                })
                .addOnFailureListener(e -> {

                    adminAppointmentsProgressBar
                            .setVisibility(View.GONE);

                    Toast.makeText(
                            ManageAppointmentsActivity.this,
                            "Failed to load appointments: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                });
    }


    // UPDATE REPAIR STATUS IN FIREBASE
    private void updateStatus(
            String appointmentId,
            String newStatus
    ) {

        firestore.collection("appointments")
                .document(appointmentId)
                .update("status", newStatus)

                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            ManageAppointmentsActivity.this,
                            "Status changed to " + newStatus,
                            Toast.LENGTH_SHORT
                    ).show();

                    loadAppointments();

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            ManageAppointmentsActivity.this,
                            "Update failed: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                });
    }
}