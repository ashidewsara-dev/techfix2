
package com.techfix.app.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.techfix.app.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RepairHistoryActivity extends AppCompatActivity {

    private LinearLayout historyContainer;
    private ProgressBar historyProgressBar;
    private TextView txtNoHistory;
    private TextView txtCompletedCount;
    private Button btnRefreshHistory;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_repair_history);

        historyContainer = findViewById(R.id.historyContainer);
        historyProgressBar = findViewById(R.id.historyProgressBar);
        txtNoHistory = findViewById(R.id.txtNoHistory);
        txtCompletedCount = findViewById(R.id.txtCompletedCount);
        btnRefreshHistory = findViewById(R.id.btnRefreshHistory);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        btnRefreshHistory.setOnClickListener(
                v -> loadRepairHistory()
        );

        loadRepairHistory();
    }

    private void loadRepairHistory() {

        if (firebaseAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please log in again",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        String customerId =
                firebaseAuth.getCurrentUser().getUid();

        historyProgressBar.setVisibility(View.VISIBLE);
        btnRefreshHistory.setEnabled(false);
        txtNoHistory.setVisibility(View.GONE);
        txtCompletedCount.setText("Loading completed repairs...");
        historyContainer.removeAllViews();

        firestore.collection("appointments")
                .whereEqualTo("customerId", customerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    historyProgressBar.setVisibility(View.GONE);
                    btnRefreshHistory.setEnabled(true);

                    List<QueryDocumentSnapshot> completedRepairs =
                            new ArrayList<>();

                    for (QueryDocumentSnapshot document
                            : queryDocumentSnapshots) {

                        String status = document.getString("status");

                        if ("Completed".equalsIgnoreCase(status)) {
                            completedRepairs.add(document);
                        }
                    }

                    // Newest completed appointments appear first,
                    // based on their original booking time.
                    Collections.sort(
                            completedRepairs,
                            (first, second) -> {

                                Long firstTime =
                                        first.getLong("createdAt");

                                Long secondTime =
                                        second.getLong("createdAt");

                                long a = firstTime == null
                                        ? 0L : firstTime;

                                long b = secondTime == null
                                        ? 0L : secondTime;

                                return Long.compare(b, a);
                            }
                    );

                    int completedCount = completedRepairs.size();

                    txtCompletedCount.setText(
                            completedCount == 1
                                    ? "1 completed repair"
                                    : completedCount + " completed repairs"
                    );

                    if (completedCount == 0) {

                        txtNoHistory.setText(
                                "No completed repairs yet.\n" +
                                        "Your repairs will appear here " +
                                        "when their status becomes Completed."
                        );

                        txtNoHistory.setVisibility(View.VISIBLE);
                        return;
                    }

                    for (QueryDocumentSnapshot document
                            : completedRepairs) {

                        addHistoryCard(document);
                    }
                })
                .addOnFailureListener(e -> {

                    historyProgressBar.setVisibility(View.GONE);
                    btnRefreshHistory.setEnabled(true);

                    txtCompletedCount.setText(
                            "Unable to load repair history"
                    );

                    txtNoHistory.setText(
                            "Could not load your history.\n" +
                                    "Tap Refresh History to try again."
                    );

                    txtNoHistory.setVisibility(View.VISIBLE);

                    Toast.makeText(
                            this,
                            "Failed to load history: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void addHistoryCard(QueryDocumentSnapshot document) {

        String category = safeValue(
                document.getString("deviceCategory")
        );

        String brand = safeValue(
                document.getString("brand")
        );

        String model = safeValue(
                document.getString("model")
        );

        String problem = safeValue(
                document.getString("problem")
        );

        String branch = safeValue(
                document.getString("branch")
        );

        String date = safeValue(
                document.getString("preferredDate")
        );

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);

        card.setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(18)
        );

        card.setBackgroundResource(R.drawable.bg_tech_card);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin = dp(16);

        historyContainer.addView(card, cardParams);

        TextView deviceTitle = new TextView(this);
        deviceTitle.setText(brand + " " + model);
        deviceTitle.setTextColor(
                getColor(R.color.tech_text)
        );
        deviceTitle.setTextSize(20);
        deviceTitle.setTypeface(null, Typeface.BOLD);

        card.addView(deviceTitle);

        TextView categoryView = new TextView(this);
        categoryView.setText(category);
        categoryView.setTextColor(
                getColor(R.color.tech_text_secondary)
        );
        categoryView.setTextSize(13);
        categoryView.setPadding(0, dp(4), 0, dp(12));

        card.addView(categoryView);

        TextView statusView = new TextView(this);
        statusView.setText("✓  COMPLETED");
        statusView.setTextColor(
                getColor(R.color.tech_teal)
        );
        statusView.setTextSize(14);
        statusView.setTypeface(null, Typeface.BOLD);
        statusView.setPadding(0, 0, 0, dp(12));

        card.addView(statusView);

        addDetail(card, "Problem", problem);
        addDetail(card, "Branch", branch);
        addDetail(card, "Preferred date", date);

        TextView idView = new TextView(this);
        idView.setText(
                "Appointment ID: " + document.getId()
        );
        idView.setTextColor(
                getColor(R.color.tech_text_secondary)
        );
        idView.setTextSize(11);
        idView.setPadding(0, dp(12), 0, 0);

        card.addView(idView);
    }

    private void addDetail(
            LinearLayout card,
            String label,
            String value
    ) {

        TextView detailView = new TextView(this);
        detailView.setText(label + ": " + value);
        detailView.setTextColor(
                getColor(R.color.tech_text)
        );
        detailView.setTextSize(14);
        detailView.setPadding(0, dp(5), 0, dp(5));

        card.addView(detailView);
    }

    private String safeValue(String value) {

        if (value == null || value.trim().isEmpty()) {
            return "Not available";
        }

        return value;
    }

    private int dp(int value) {

        return Math.round(
                value * getResources().getDisplayMetrics().density
        );
    }
}
