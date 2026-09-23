
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
import java.util.Locale;

import androidx.appcompat.app.AlertDialog;


public class TrackRepairActivity extends AppCompatActivity {

    private LinearLayout repairContainer;
    private ProgressBar progressBar;
    private TextView txtNoRepairs;
    private Button btnRefresh;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_track_repair);

        repairContainer = findViewById(R.id.repairContainer);
        progressBar = findViewById(R.id.progressBar);
        txtNoRepairs = findViewById(R.id.txtNoRepairs);
        btnRefresh = findViewById(R.id.btnRefresh);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        btnRefresh.setOnClickListener(v -> loadRepairs());

        loadRepairs();
    }

    private void loadRepairs() {

        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(
                    this,
                    "Please log in again",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        String customerId = firebaseAuth.getCurrentUser().getUid();

        progressBar.setVisibility(View.VISIBLE);
        btnRefresh.setEnabled(false);
        txtNoRepairs.setVisibility(View.GONE);
        repairContainer.removeAllViews();

        firestore.collection("appointments")
                .whereEqualTo("customerId", customerId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    progressBar.setVisibility(View.GONE);
                    btnRefresh.setEnabled(true);

                    if (queryDocumentSnapshots.isEmpty()) {
                        txtNoRepairs.setText(
                                "No repair appointments found.\n" +
                                        "Book a repair to see its status here."
                        );
                        txtNoRepairs.setVisibility(View.VISIBLE);
                        return;
                    }

                    List<QueryDocumentSnapshot> repairs =
                            new ArrayList<>();

                    for (QueryDocumentSnapshot document
                            : queryDocumentSnapshots) {
                        repairs.add(document);
                    }

                    // Show the newest appointments first.
                    Collections.sort(repairs, (first, second) -> {

                        Long firstTime = first.getLong("createdAt");
                        Long secondTime = second.getLong("createdAt");

                        long a = firstTime == null ? 0L : firstTime;
                        long b = secondTime == null ? 0L : secondTime;

                        return Long.compare(b, a);
                    });

                    for (QueryDocumentSnapshot document : repairs) {
                        addRepairCard(document);
                    }
                })
                .addOnFailureListener(e -> {

                    progressBar.setVisibility(View.GONE);
                    btnRefresh.setEnabled(true);

                    txtNoRepairs.setText(
                            "Unable to load your repairs.\n" +
                                    "Tap Refresh to try again."
                    );
                    txtNoRepairs.setVisibility(View.VISIBLE);

                    Toast.makeText(
                            this,
                            "Failed to load repairs: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void addRepairCard(QueryDocumentSnapshot document) {

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

        String status = safeValue(
                document.getString("status")
        );

        String appointmentId = document.getId();

        Double paymentAmount = document.getDouble("paymentAmount");
        String paymentStatus = document.getString("paymentStatus");

        if (paymentStatus == null || paymentStatus.trim().isEmpty()) {
            paymentStatus = "Unpaid";
        }


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
        repairContainer.addView(card, cardParams);

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
        statusView.setText(
                "STATUS  •  " + status.toUpperCase(Locale.ROOT)
        );
        statusView.setTextColor(statusColor(status));
        statusView.setTextSize(14);
        statusView.setTypeface(null, Typeface.BOLD);
        statusView.setPadding(0, 0, 0, dp(12));
        card.addView(statusView);

        addDetail(card, "Problem", problem);
        addDetail(card, "Branch", branch);
        addDetail(card, "Preferred date", date);

        TextView idView = new TextView(this);
        idView.setText("Appointment ID: " + appointmentId);
        idView.setTextColor(
                getColor(R.color.tech_text_secondary)
        );
        idView.setTextSize(11);
        idView.setPadding(0, dp(12), 0, 0);
        card.addView(idView);


        TextView paymentTitle = new TextView(this);
        paymentTitle.setText("PAYMENT");
        paymentTitle.setTextSize(13);
        paymentTitle.setTypeface(null, Typeface.BOLD);
        paymentTitle.setTextColor(getColor(R.color.tech_teal));
        paymentTitle.setPadding(0, dp(20), 0, dp(8));
        card.addView(paymentTitle);

        if (paymentAmount == null || paymentAmount <= 0) {

            addDetail(card, "Repair amount", "Awaiting final amount from admin");

        } else {

            String formattedAmount = String.format(
                    Locale.US,
                    "LKR %,.2f",
                    paymentAmount
            );

            addDetail(card, "Repair amount", formattedAmount);
            addDetail(card, "Payment status", paymentStatus);

            if (!"Paid (Demo)".equalsIgnoreCase(paymentStatus)) {

                Button btnPay = new Button(this);
                btnPay.setText("Pay Now (Demo)");
                btnPay.setAllCaps(false);
                btnPay.setTextColor(getColor(R.color.tech_background));
                btnPay.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                getColor(R.color.tech_teal)
                        )
                );

                card.addView(btnPay);

                btnPay.setOnClickListener(v ->
                        showDemoPaymentDialog(
                                appointmentId,
                                formattedAmount
                        )
                );
            }
        }

    }


    private void showDemoPaymentDialog(
            String appointmentId,
            String formattedAmount
    ) {

        new AlertDialog.Builder(this)
                .setTitle("Demo Payment")
                .setMessage(
                        "Repair amount: " + formattedAmount
                                + "\n\nThis is a simulated payment "
                                + "for your coursework demonstration. "
                                + "No real money will be charged."
                )
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Confirm Demo Payment",
                        (dialog, which) -> completeDemoPayment(appointmentId))
                .show();
    }

    private void completeDemoPayment(String appointmentId) {

        if (firebaseAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please log in again",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String currentCustomerId =
                firebaseAuth.getCurrentUser().getUid();

        firestore.collection("appointments")
                .document(appointmentId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {

                        Toast.makeText(
                                this,
                                "Repair appointment not found",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    String ownerId = document.getString("customerId");
                    Double amount = document.getDouble("paymentAmount");
                    String status = document.getString("paymentStatus");

                    if (!currentCustomerId.equals(ownerId)) {

                        Toast.makeText(
                                this,
                                "This appointment does not belong to your account",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    if (amount == null || amount <= 0) {

                        Toast.makeText(
                                this,
                                "Repair amount has not been set",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadRepairs();
                        return;
                    }

                    if ("Paid ".equalsIgnoreCase(status)) {

                        Toast.makeText(
                                this,
                                "This payment is already completed",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadRepairs();
                        return;
                    }

                    firestore.collection("appointments")
                            .document(appointmentId)
                            .update("paymentStatus", "Paid")
                            .addOnSuccessListener(unused -> {

                                Toast.makeText(
                                        this,
                                        "Payment completed",
                                        Toast.LENGTH_SHORT
                                ).show();

                                loadRepairs();
                            })
                            .addOnFailureListener(e ->

                                    Toast.makeText(
                                            this,
                                            "Payment update failed: "
                                                    + e.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show()
                            );
                })
                .addOnFailureListener(e ->

                        Toast.makeText(
                                this,
                                "Could not verify appointment: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
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

    private int statusColor(String status) {

        String normalized = status.toLowerCase(Locale.ROOT);

        if (normalized.contains("complete")
                || normalized.contains("ready")
                || normalized.contains("deliver")) {

            return getColor(R.color.tech_teal);
        }

        if (normalized.contains("pending")
                || normalized.contains("progress")
                || normalized.contains("repair")) {

            return getColor(R.color.tech_warning);
        }

        return getColor(R.color.tech_text_secondary);
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
