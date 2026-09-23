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

public class RepairHistoryActivity extends AppCompatActivity {

    private LinearLayout historyContainer;
    private ProgressBar historyProgressBar;
    private TextView txtNoHistory;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_repair_history);

        historyContainer = findViewById(R.id.historyContainer);
        historyProgressBar = findViewById(R.id.historyProgressBar);
        txtNoHistory = findViewById(R.id.txtNoHistory);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        loadRepairHistory();
    }


    private void loadRepairHistory() {

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

        historyProgressBar.setVisibility(View.VISIBLE);


        firestore
                .collection("appointments")
                .whereEqualTo("customerId", customerId)
                .get()

                .addOnSuccessListener(queryDocumentSnapshots -> {

                    historyProgressBar.setVisibility(View.GONE);
                    historyContainer.removeAllViews();

                    int completedCount = 0;

                    for (QueryDocumentSnapshot document
                            : queryDocumentSnapshots) {

                        String status =
                                document.getString("status");

                        if ("Completed".equalsIgnoreCase(status)) {

                            completedCount++;

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

                            TextView historyView =
                                    new TextView(
                                            RepairHistoryActivity.this
                                    );

                            String details =
                                    "Device: " + brand + " " + model
                                            + "\nCategory: " + category
                                            + "\nProblem: " + problem
                                            + "\nBranch: " + branch
                                            + "\nDate: " + date
                                            + "\nStatus: Completed";

                            historyView.setText(details);
                            historyView.setTextSize(16);

                            historyView.setPadding(
                                    30,
                                    30,
                                    30,
                                    30
                            );

                            historyContainer.addView(historyView);
                        }
                    }


                    if (completedCount == 0) {

                        txtNoHistory.setVisibility(View.VISIBLE);

                    } else {

                        txtNoHistory.setVisibility(View.GONE);
                    }

                })

                .addOnFailureListener(e -> {

                    historyProgressBar.setVisibility(View.GONE);

                    Toast.makeText(
                            RepairHistoryActivity.this,
                            "Failed to load history: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                });
    }
}