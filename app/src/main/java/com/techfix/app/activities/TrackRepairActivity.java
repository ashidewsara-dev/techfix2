package com.techfix.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.techfix.app.R;

public class TrackRepairActivity extends AppCompatActivity {

    private LinearLayout repairContainer;
    private ProgressBar progressBar;
    private TextView txtNoRepairs;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_track_repair);

        repairContainer = findViewById(R.id.repairContainer);
        progressBar = findViewById(R.id.progressBar);
        txtNoRepairs = findViewById(R.id.txtNoRepairs);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        loadRepairs();
    }


    private void loadRepairs() {

        if (firebaseAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        String customerId =
                firebaseAuth.getCurrentUser().getUid();

        progressBar.setVisibility(View.VISIBLE);


        firestore
                .collection("appointments")
                .whereEqualTo("customerId", customerId)
                .get()

                .addOnSuccessListener(queryDocumentSnapshots -> {

                    progressBar.setVisibility(View.GONE);

                    repairContainer.removeAllViews();


                    if (queryDocumentSnapshots.isEmpty()) {

                        txtNoRepairs.setVisibility(View.VISIBLE);
                        return;
                    }


                    txtNoRepairs.setVisibility(View.GONE);


                    for (QueryDocumentSnapshot document
                            : queryDocumentSnapshots) {


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


                        TextView repairView =
                                new TextView(
                                        TrackRepairActivity.this
                                );


                        String repairDetails =

                                "Device: "
                                        + brand
                                        + " "
                                        + model

                                        + "\nCategory: "
                                        + category

                                        + "\nProblem: "
                                        + problem

                                        + "\nBranch: "
                                        + branch

                                        + "\nDate: "
                                        + date

                                        + "\nStatus: "
                                        + status;


                        repairView.setText(repairDetails);

                        repairView.setTextSize(16);

                        repairView.setPadding(
                                30,
                                30,
                                30,
                                30
                        );


                        repairContainer.addView(
                                repairView
                        );
                    }

                })

                .addOnFailureListener(e -> {

                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(
                            TrackRepairActivity.this,
                            "Failed to load repairs: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                });
    }
}