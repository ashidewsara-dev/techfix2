package com.techfix.app.activities;

import android.app.AlertDialog;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import android.text.InputType;
import android.widget.EditText;
import java.util.Locale;


public class ManageAppointmentsActivity extends AppCompatActivity {

    private LinearLayout adminAppointmentsContainer;
    private ProgressBar adminAppointmentsProgressBar;
    private TextView txtNoAppointments;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(
                getResources().getColor(R.color.tech_background)
        );

        getWindow().setNavigationBarColor(
                getResources().getColor(R.color.tech_background)
        );

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

    private int dp(int value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private TextView createText(
            String value,
            int size,
            int color,
            boolean bold
    ) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(size);
        text.setTextColor(color);

        if (bold) {
            text.setTypeface(null, android.graphics.Typeface.BOLD);
        }

        return text;
    }

    private Button createButton(String label) {
        Button button = new Button(this);

        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(14);
        button.setTextColor(
                getResources().getColor(R.color.tech_background)
        );
        button.setBackgroundTintList(
                getResources().getColorStateList(R.color.tech_teal)
        );

        return button;
    }

    private void loadAppointments() {

        adminAppointmentsProgressBar.setVisibility(View.VISIBLE);

        firestore.collection("appointments")
                .get()
                .addOnSuccessListener(snapshots -> {

                    adminAppointmentsProgressBar.setVisibility(View.GONE);
                    adminAppointmentsContainer.removeAllViews();

                    if (snapshots.isEmpty()) {
                        txtNoAppointments.setVisibility(View.VISIBLE);
                        return;
                    }

                    txtNoAppointments.setVisibility(View.GONE);

                    for (QueryDocumentSnapshot document : snapshots) {

                        String appointmentId = document.getId();

                        String category = document.getString("deviceCategory");
                        String brand = document.getString("brand");
                        String model = document.getString("model");
                        String problem = document.getString("problem");
                        String branch = document.getString("branch");
                        String date = document.getString("preferredDate");
                        String status = document.getString("status");
                        String technicianId = document.getString("technicianId");

                        Double paymentAmount = document.getDouble("paymentAmount");
                        String paymentStatus = document.getString("paymentStatus");

                        if (paymentStatus == null || paymentStatus.trim().isEmpty()) {
                            paymentStatus = "Unpaid";
                        }


                        // DARK REPAIR CARD
                        LinearLayout card = new LinearLayout(this);
                        card.setOrientation(LinearLayout.VERTICAL);
                        card.setPadding(
                                dp(18), dp(18), dp(18), dp(18)
                        );
                        card.setBackgroundResource(R.drawable.bg_tech_card);

                        LinearLayout.LayoutParams cardParams =
                                new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );

                        cardParams.bottomMargin = dp(16);
                        card.setLayoutParams(cardParams);

                        // DEVICE TITLE
                        TextView deviceTitle = createText(
                                brand + " " + model,
                                20,
                                getResources().getColor(R.color.tech_text),
                                true
                        );

                        card.addView(deviceTitle);

                        // CATEGORY
                        TextView categoryText = createText(
                                category == null ? "Repair Request" : category,
                                13,
                                getResources().getColor(
                                        R.color.tech_text_secondary
                                ),
                                false
                        );

                        categoryText.setPadding(0, dp(4), 0, dp(12));
                        card.addView(categoryText);

                        // STATUS BADGE
                        TextView statusBadge = createText(
                                "●  " + (status == null ? "Pending" : status),
                                14,
                                getResources().getColor(R.color.tech_warning),
                                true
                        );

                        if ("Completed".equalsIgnoreCase(status)) {
                            statusBadge.setTextColor(
                                    getResources().getColor(R.color.tech_teal)
                            );
                        } else if ("In Progress".equalsIgnoreCase(status)) {
                            statusBadge.setTextColor(0xFF79B8FF);
                        }

                        card.addView(statusBadge);

                        // REPAIR DETAILS
                        String details =
                                "\nProblem: " + problem
                                        + "\nBranch: " + branch
                                        + "\nPreferred Date: " + date
                                        + "\nTechnician: "
                                        + ((technicianId == null
                                        || technicianId.isEmpty())
                                        ? "Not assigned" : "Assigned");

                        TextView detailsView = createText(
                                details,
                                14,
                                getResources().getColor(
                                        R.color.tech_text_secondary
                                ),
                                false
                        );

                        detailsView.setLineSpacing(dp(4), 1f);
                        card.addView(detailsView);

                        // BUTTONS
                        Button btnPending = createButton("Set Pending");
                        Button btnInProgress = createButton("Set In Progress");
                        Button btnCompleted = createButton("Set Completed");
                        Button btnAssign = createButton("Assign Technician");


                        Button btnSetPayment = createButton("Set Repair Amount");

                        String amountText = paymentAmount == null
                                ? "Not set"
                                : String.format(Locale.US, "LKR %,.2f", paymentAmount);

                        TextView paymentView = createText(
                                "\nPayment amount: " + amountText
                                        + "\nPayment status: " + paymentStatus,
                                14,
                                getColor(R.color.tech_text_secondary),
                                false
                        );


                        btnPending.setOnClickListener(v ->
                                updateStatus(appointmentId, "Pending")
                        );

                        btnInProgress.setOnClickListener(v ->
                                updateStatus(appointmentId, "In Progress")
                        );

                        btnCompleted.setOnClickListener(v ->
                                updateStatus(appointmentId, "Completed")
                        );

                        btnAssign.setOnClickListener(v ->
                                showTechnicianPicker(appointmentId, branch)
                        );

                        card.addView(btnPending);
                        card.addView(btnInProgress);
                        card.addView(btnCompleted);
                        card.addView(btnAssign);


                        card.addView(paymentView);
                        card.addView(btnSetPayment);

                        btnSetPayment.setOnClickListener(v ->
                                showPaymentAmountDialog(appointmentId)
                        );


                        adminAppointmentsContainer.addView(card);
                    }
                })
                .addOnFailureListener(e -> {
                    adminAppointmentsProgressBar.setVisibility(View.GONE);

                    Toast.makeText(
                            this,
                            "Failed to load appointments: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void showPaymentAmountDialog(String appointmentId) {


            EditText amountInput = new EditText(this);
            amountInput.setHint("Amount in LKR");
            amountInput.setInputType(
                    InputType.TYPE_CLASS_NUMBER
                            | InputType.TYPE_NUMBER_FLAG_DECIMAL
            );

            LinearLayout container = new LinearLayout(this);
            container.setPadding(dp(20), dp(8), dp(20), 0);
            container.addView(amountInput);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Set Repair Amount")
                    .setMessage(
                            "Enter the final repair amount. " +
                                    "This is for demo payments only."
                    )
                    .setView(container)
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Save", null)
                    .create();

            dialog.setOnShowListener(unused -> {

                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(v -> {

                            String input = amountInput.getText()
                                    .toString().trim();

                            double amount;

                            try {
                                amount = Double.parseDouble(input);
                            } catch (NumberFormatException e) {
                                amountInput.setError("Enter a valid amount");
                                return;
                            }

                            if (!Double.isFinite(amount) || amount <= 0) {
                                amountInput.setError(
                                        "Enter an amount greater than zero"
                                );
                                return;
                            }

                            dialog.getButton(
                                    AlertDialog.BUTTON_POSITIVE
                            ).setEnabled(false);

                            Map<String, Object> updates = new HashMap<>();
                            updates.put("paymentAmount", amount);
                            updates.put("paymentStatus", "Unpaid");

                            firestore.collection("appointments")
                                    .document(appointmentId)
                                    .update(updates)
                                    .addOnSuccessListener(result -> {

                                        dialog.dismiss();

                                        Toast.makeText(
                                                this,
                                                "Repair amount saved",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        loadAppointments();
                                    })
                                    .addOnFailureListener(e -> {

                                        dialog.getButton(
                                                AlertDialog.BUTTON_POSITIVE
                                        ).setEnabled(true);

                                        Toast.makeText(
                                                this,
                                                "Could not save amount: "
                                                        + e.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show();
                                    });
                        });
            });

            dialog.show();
        }

    private void updateStatus(String appointmentId, String newStatus) {

        firestore.collection("appointments")
                .document(appointmentId)
                .update("status", newStatus)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "Status updated to " + newStatus,
                            Toast.LENGTH_SHORT
                    ).show();

                    loadAppointments();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Update failed: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    private void showTechnicianPicker(
            String appointmentId,
            String appointmentBranch
    ) {

        firestore.collection("users")
                .whereEqualTo("role", "technician")
                .get()
                .addOnSuccessListener(snapshots -> {

                    ArrayList<String> technicianNames = new ArrayList<>();
                    ArrayList<String> technicianIds = new ArrayList<>();

                    for (QueryDocumentSnapshot document : snapshots) {

                        String branch = document.getString("branch");

                        if (appointmentBranch == null
                                || !appointmentBranch.equalsIgnoreCase(branch)) {
                            continue;
                        }

                        String name = document.getString("name");

                        technicianNames.add(
                                name == null ? "Unnamed Technician" : name
                        );

                        technicianIds.add(document.getId());
                    }

                    if (technicianIds.isEmpty()) {

                        Toast.makeText(
                                this,
                                "No technicians found for " + appointmentBranch,
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Assign Technician")
                            .setItems(
                                    technicianNames.toArray(new String[0]),
                                    (dialog, which) -> {

                                        String selectedId =
                                                technicianIds.get(which);

                                        Map<String, Object> updates =
                                                new HashMap<>();

                                        updates.put("technicianId", selectedId);

                                        firestore.collection("appointments")
                                                .document(appointmentId)
                                                .update(updates)
                                                .addOnSuccessListener(unused -> {

                                                    Toast.makeText(
                                                            this,
                                                            "Technician assigned successfully",
                                                            Toast.LENGTH_SHORT
                                                    ).show();

                                                    loadAppointments();
                                                })
                                                .addOnFailureListener(e ->
                                                        Toast.makeText(
                                                                this,
                                                                "Assignment failed: "
                                                                        + e.getMessage(),
                                                                Toast.LENGTH_LONG
                                                        ).show()
                                                );
                                    }
                            )
                            .setNegativeButton("Cancel", null)
                            .show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Failed to load technicians: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }
}

