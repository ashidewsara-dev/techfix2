
package com.techfix.app.activities;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.techfix.app.R;

public class AssignedRepairsActivity extends AppCompatActivity {

    private TextView txtAssignedRepairCount;
    private TextView txtNoAssignedRepairs;
    private ProgressBar assignedRepairsProgressBar;
    private LinearLayout assignedRepairsContainer;
    private Button btnRefreshAssignedRepairs;

    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_assigned_repairs);

        txtAssignedRepairCount =
                findViewById(R.id.txtAssignedRepairCount);

        txtNoAssignedRepairs =
                findViewById(R.id.txtNoAssignedRepairs);

        assignedRepairsProgressBar =
                findViewById(R.id.assignedRepairsProgressBar);

        assignedRepairsContainer =
                findViewById(R.id.assignedRepairsContainer);

        btnRefreshAssignedRepairs =
                findViewById(R.id.btnRefreshAssignedRepairs);

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        btnRefreshAssignedRepairs.setOnClickListener(
                v -> loadAssignedRepairs()
        );

        loadAssignedRepairs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAssignedRepairs();
    }

    private void loadAssignedRepairs() {

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {

            txtAssignedRepairCount.setText("Your Repairs");

            txtNoAssignedRepairs.setText(
                    "Please log in to view your assigned repairs."
            );

            txtNoAssignedRepairs.setVisibility(View.VISIBLE);
            assignedRepairsContainer.removeAllViews();

            return;
        }

        String technicianId = currentUser.getUid();

        assignedRepairsProgressBar.setVisibility(View.VISIBLE);
        btnRefreshAssignedRepairs.setEnabled(false);
        txtNoAssignedRepairs.setVisibility(View.GONE);
        assignedRepairsContainer.removeAllViews();

        firestore.collection("appointments")
                .whereEqualTo("technicianId", technicianId)
                .get()
                .addOnSuccessListener(snapshots -> {

                    assignedRepairsProgressBar.setVisibility(View.GONE);
                    btnRefreshAssignedRepairs.setEnabled(true);

                    int count = snapshots.size();

                    txtAssignedRepairCount.setText(
                            count == 1
                                    ? "1 Assigned Repair"
                                    : count + " Assigned Repairs"
                    );

                    if (count == 0) {

                        txtNoAssignedRepairs.setText(
                                "No repairs assigned to you yet."
                        );

                        txtNoAssignedRepairs.setVisibility(View.VISIBLE);
                        return;
                    }

                    for (DocumentSnapshot document
                            : snapshots.getDocuments()) {

                        addRepairCard(document);
                    }
                })
                .addOnFailureListener(e -> {

                    assignedRepairsProgressBar.setVisibility(View.GONE);
                    btnRefreshAssignedRepairs.setEnabled(true);

                    txtAssignedRepairCount.setText(
                            "Assigned Repairs Unavailable"
                    );

                    txtNoAssignedRepairs.setText(
                            "Could not load repairs. Tap Refresh."
                    );

                    txtNoAssignedRepairs.setVisibility(View.VISIBLE);

                    showError("Could not load assigned repairs", e);
                });
    }

    private void addRepairCard(DocumentSnapshot document) {

        String appointmentId = document.getId();

        String brand = safeValue(document.getString("brand"));
        String model = safeValue(document.getString("model"));

        String category = safeValue(
                document.getString("deviceCategory")
        );

        String problem = safeValue(document.getString("problem"));
        String branch = safeValue(document.getString("branch"));

        String date = safeValue(
                document.getString("preferredDate")
        );

        // The compressed photo saved by BookRepairActivity.
        String imageBase64 =
                document.getString("deviceImageBase64");

        String status = document.getString("status");

        if (status == null || status.trim().isEmpty()) {
            status = "Pending";
        }

        final String currentStatus = status;

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

        cardParams.bottomMargin = dp(14);

        assignedRepairsContainer.addView(card, cardParams);

        // DEVICE TITLE

        TextView deviceTitle = new TextView(this);

        deviceTitle.setText(brand + " " + model);
        deviceTitle.setTextSize(19);
        deviceTitle.setTypeface(null, Typeface.BOLD);
        deviceTitle.setTextColor(getColor(R.color.tech_text));

        card.addView(deviceTitle);

        // REPAIR DETAILS

        addDetail(card, "Category", category);
        addDetail(card, "Problem", problem);
        addDetail(card, "Branch", branch);
        addDetail(card, "Preferred date", date);

        // DEVICE PHOTO

        TextView photoTitle = new TextView(this);
        photoTitle.setText("Device Photo");
        photoTitle.setTextSize(15);
        photoTitle.setTypeface(null, Typeface.BOLD);
        photoTitle.setTextColor(getColor(R.color.tech_text));
        photoTitle.setPadding(0, dp(14), 0, dp(8));

        card.addView(photoTitle);

        if (imageBase64 == null || imageBase64.trim().isEmpty()) {

            addDetail(
                    card,
                    "Photo",
                    "No device photo attached to this booking"
            );

        } else {

            try {

                byte[] imageBytes = Base64.decode(
                        imageBase64,
                        Base64.DEFAULT
                );

                Bitmap deviceBitmap =
                        BitmapFactory.decodeByteArray(
                                imageBytes,
                                0,
                                imageBytes.length
                        );

                if (deviceBitmap == null) {
                    throw new IllegalArgumentException(
                            "Image could not be decoded"
                    );
                }

                ImageView deviceImage = new ImageView(this);

                LinearLayout.LayoutParams imageParams =
                        new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                dp(190)
                        );

                deviceImage.setLayoutParams(imageParams);
                deviceImage.setScaleType(
                        ImageView.ScaleType.FIT_CENTER
                );

                deviceImage.setImageBitmap(deviceBitmap);

                card.addView(deviceImage);

                // Tap the photo to view it in a larger dialog.
                deviceImage.setOnClickListener(v -> {

                    ImageView largeImage = new ImageView(this);

                    largeImage.setImageBitmap(deviceBitmap);
                    largeImage.setScaleType(
                            ImageView.ScaleType.FIT_CENTER
                    );

                    LinearLayout imageContainer =
                            new LinearLayout(this);

                    imageContainer.setPadding(
                            dp(12), dp(12), dp(12), dp(12)
                    );

                    imageContainer.addView(
                            largeImage,
                            new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    dp(320)
                            )
                    );

                    new AlertDialog.Builder(this)
                            .setTitle("Device Photo")
                            .setView(imageContainer)
                            .setPositiveButton("Close", null)
                            .show();
                });

            } catch (Exception e) {

                addDetail(
                        card,
                        "Photo",
                        "Could not display the attached photo"
                );
            }
        }

        // REPAIR STATUS

        TextView statusText = new TextView(this);

        statusText.setText("●  " + currentStatus);
        statusText.setTextSize(15);
        statusText.setTypeface(null, Typeface.BOLD);

        statusText.setPadding(
                0, dp(12), 0, dp(12)
        );

        if ("Completed".equalsIgnoreCase(currentStatus)) {

            statusText.setTextColor(
                    getColor(R.color.tech_teal)
            );

        } else if ("In Progress".equalsIgnoreCase(currentStatus)) {

            statusText.setTextColor(0xFF79B8FF);

        } else {

            statusText.setTextColor(
                    getColor(R.color.tech_warning)
            );
        }

        card.addView(statusText);

        // UPDATE STATUS BUTTON

        Button btnUpdateStatus = new Button(this);

        btnUpdateStatus.setText("Update Repair Status");
        btnUpdateStatus.setAllCaps(false);

        btnUpdateStatus.setTextColor(
                getColor(R.color.tech_background)
        );

        btnUpdateStatus.setBackgroundTintList(
                ColorStateList.valueOf(
                        getColor(R.color.tech_teal)
                )
        );

        card.addView(btnUpdateStatus);

        btnUpdateStatus.setOnClickListener(v ->
                showStatusPicker(
                        appointmentId,
                        currentStatus
                )
        );
    }

    private void showStatusPicker(
            String appointmentId,
            String currentStatus
    ) {

        String[] statuses = {
                "Pending",
                "In Progress",
                "Completed"
        };

        new AlertDialog.Builder(this)
                .setTitle("Update Repair Status")
                .setSingleChoiceItems(
                        statuses,
                        statusIndex(statuses, currentStatus),
                        (dialog, which) -> {

                            dialog.dismiss();

                            String selectedStatus =
                                    statuses[which];

                            if (selectedStatus.equalsIgnoreCase(
                                    currentStatus
                            )) {
                                return;
                            }

                            updateRepairStatus(
                                    appointmentId,
                                    selectedStatus
                            );
                        }
                )
                .setNegativeButton("Cancel", null)
                .show();
    }

    private int statusIndex(
            String[] statuses,
            String currentStatus
    ) {

        for (int i = 0; i < statuses.length; i++) {

            if (statuses[i].equalsIgnoreCase(currentStatus)) {
                return i;
            }
        }

        return -1;
    }

    private void updateRepairStatus(
            String appointmentId,
            String newStatus
    ) {

        FirebaseUser currentUser =
                firebaseAuth.getCurrentUser();

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "Please log in again",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Check the assignment again before updating.
        firestore.collection("appointments")
                .document(appointmentId)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {

                        Toast.makeText(
                                this,
                                "This repair no longer exists",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadAssignedRepairs();
                        return;
                    }

                    String assignedId =
                            document.getString("technicianId");

                    if (!currentUser.getUid().equals(assignedId)) {

                        Toast.makeText(
                                this,
                                "This repair is no longer assigned to you",
                                Toast.LENGTH_LONG
                        ).show();

                        loadAssignedRepairs();
                        return;
                    }

                    firestore.collection("appointments")
                            .document(appointmentId)
                            .update("status", newStatus)
                            .addOnSuccessListener(unused -> {

                                Toast.makeText(
                                        this,
                                        "Status updated to " + newStatus,
                                        Toast.LENGTH_SHORT
                                ).show();

                                loadAssignedRepairs();
                            })
                            .addOnFailureListener(e ->
                                    showError(
                                            "Could not update repair status",
                                            e
                                    )
                            );
                })
                .addOnFailureListener(e ->
                        showError(
                                "Could not verify repair assignment",
                                e
                        )
                );
    }

    private void addDetail(
            LinearLayout card,
            String label,
            String value
    ) {

        TextView detail = new TextView(this);

        detail.setText(label + ": " + value);
        detail.setTextSize(14);

        detail.setTextColor(
                getColor(R.color.tech_text_secondary)
        );

        detail.setPadding(0, dp(7), 0, 0);

        card.addView(detail);
    }

    private String safeValue(String value) {

        if (value == null || value.trim().isEmpty()) {
            return "Not available";
        }

        return value;
    }

    private int dp(int value) {

        return Math.round(
                value * getResources()
                        .getDisplayMetrics().density
        );
    }

    private void showError(String message, Exception e) {

        Toast.makeText(
                this,
                message + ": " + e.getMessage(),
                Toast.LENGTH_LONG
        ).show();
    }
}
