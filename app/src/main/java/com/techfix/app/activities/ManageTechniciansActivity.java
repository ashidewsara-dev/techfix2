
package com.techfix.app.activities;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.techfix.app.R;

import java.util.HashMap;
import java.util.Map;

public class ManageTechniciansActivity extends AppCompatActivity {

    private EditText editTechnicianEmail;
    private EditText editTechnicianName;
    private EditText editTechnicianPhone;

    private EditText editTechnicianBranch;


    private TextView txtTechnicianFormTitle;
    private TextView txtTechnicianCount;
    private TextView txtNoTechnicians;

    private Button btnSaveTechnician;
    private Button btnCancelTechnicianEdit;
    private Button btnRefreshTechnicians;

    private ProgressBar techniciansProgressBar;
    private LinearLayout techniciansContainer;

    private FirebaseFirestore firestore;

    private String editingTechnicianId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_manage_technicians);

        editTechnicianEmail =
                findViewById(R.id.editTechnicianEmail);

        editTechnicianName =
                findViewById(R.id.editTechnicianName);

        editTechnicianPhone =
                findViewById(R.id.editTechnicianPhone);

        editTechnicianBranch =
                findViewById(R.id.editTechnicianBranch);


        txtTechnicianFormTitle =
                findViewById(R.id.txtTechnicianFormTitle);

        txtTechnicianCount =
                findViewById(R.id.txtTechnicianCount);

        txtNoTechnicians =
                findViewById(R.id.txtNoTechnicians);

        btnSaveTechnician =
                findViewById(R.id.btnSaveTechnician);

        btnCancelTechnicianEdit =
                findViewById(R.id.btnCancelTechnicianEdit);

        btnRefreshTechnicians =
                findViewById(R.id.btnRefreshTechnicians);

        techniciansProgressBar =
                findViewById(R.id.techniciansProgressBar);

        techniciansContainer =
                findViewById(R.id.techniciansContainer);

        firestore = FirebaseFirestore.getInstance();

        btnSaveTechnician.setOnClickListener(
                v -> saveTechnician()
        );

        btnCancelTechnicianEdit.setOnClickListener(
                v -> clearForm()
        );

        btnRefreshTechnicians.setOnClickListener(
                v -> loadTechnicians()
        );

        loadTechnicians();
    }

    private void saveTechnician() {

        String email = editTechnicianEmail.getText()
                .toString().trim();

        String name = editTechnicianName.getText()
                .toString().trim();

        String phone = editTechnicianPhone.getText()
                .toString().trim();

        String branch = editTechnicianBranch.getText()
                .toString().trim();

        if (email.isEmpty()) {
            editTechnicianEmail.setError("Enter an email");
            return;
        }

        if (name.isEmpty()) {
            editTechnicianName.setError("Enter a name");
            return;
        }

        if (phone.isEmpty()) {
            editTechnicianPhone.setError("Enter a phone number");
            return;
        }



        if (branch.isEmpty()) {
            editTechnicianBranch.setError("Enter a branch");
            return;
        }


        btnSaveTechnician.setEnabled(false);

        if (editingTechnicianId != null) {

            Map<String, Object> updates = new HashMap<>();
            updates.put("name", name);
            updates.put("phone", phone);
            updates.put("branch", branch);


            firestore.collection("users")
                    .document(editingTechnicianId)
                    .update(updates)
                    .addOnSuccessListener(unused -> {

                        btnSaveTechnician.setEnabled(true);

                        Toast.makeText(
                                this,
                                "Technician updated",
                                Toast.LENGTH_SHORT
                        ).show();

                        clearForm();
                        loadTechnicians();
                    })
                    .addOnFailureListener(e -> {

                        btnSaveTechnician.setEnabled(true);
                        showError("Could not update technician", e);
                    });

            return;
        }

        // Find an existing registered user.
        firestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (querySnapshot.isEmpty()) {

                        btnSaveTechnician.setEnabled(true);

                        Toast.makeText(
                                this,
                                "No registered user found with that email",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    DocumentSnapshot user =
                            querySnapshot.getDocuments().get(0);

                    String currentRole = user.getString("role");

                    if ("admin".equalsIgnoreCase(currentRole)) {

                        btnSaveTechnician.setEnabled(true);

                        Toast.makeText(
                                this,
                                "An admin account cannot be changed here",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    if ("technician".equalsIgnoreCase(currentRole)) {

                        btnSaveTechnician.setEnabled(true);

                        Toast.makeText(
                                this,
                                "This user is already a technician",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("name", name);
                    updates.put("phone", phone);
                    updates.put("role", "technician");
                    updates.put("branch", branch);


                    firestore.collection("users")
                            .document(user.getId())
                            .update(updates)
                            .addOnSuccessListener(unused -> {

                                btnSaveTechnician.setEnabled(true);

                                Toast.makeText(
                                        this,
                                        "Technician role assigned",
                                        Toast.LENGTH_SHORT
                                ).show();

                                clearForm();
                                loadTechnicians();
                            })
                            .addOnFailureListener(e -> {

                                btnSaveTechnician.setEnabled(true);

                                showError(
                                        "Could not assign technician role",
                                        e
                                );
                            });
                })
                .addOnFailureListener(e -> {

                    btnSaveTechnician.setEnabled(true);

                    showError("Could not find user", e);
                });
    }

    private void loadTechnicians() {

        techniciansProgressBar.setVisibility(View.VISIBLE);
        btnRefreshTechnicians.setEnabled(false);
        txtNoTechnicians.setVisibility(View.GONE);

        techniciansContainer.removeAllViews();

        firestore.collection("users")
                .whereEqualTo("role", "technician")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    techniciansProgressBar.setVisibility(View.GONE);
                    btnRefreshTechnicians.setEnabled(true);

                    int count = querySnapshot.size();

                    txtTechnicianCount.setText(
                            count == 1
                                    ? "1 Technician"
                                    : count + " Technicians"
                    );

                    if (count == 0) {

                        txtNoTechnicians.setText(
                                "No technicians found.\n" +
                                        "Assign an existing user as a technician."
                        );

                        txtNoTechnicians.setVisibility(View.VISIBLE);
                        return;
                    }

                    for (DocumentSnapshot document
                            : querySnapshot.getDocuments()) {

                        addTechnicianCard(document);
                    }
                })
                .addOnFailureListener(e -> {

                    techniciansProgressBar.setVisibility(View.GONE);
                    btnRefreshTechnicians.setEnabled(true);

                    txtTechnicianCount.setText(
                            "Technicians unavailable"
                    );

                    txtNoTechnicians.setText(
                            "Could not load technicians. Tap Refresh."
                    );

                    txtNoTechnicians.setVisibility(View.VISIBLE);

                    showError("Could not load technicians", e);
                });
    }

    private void addTechnicianCard(DocumentSnapshot document) {

        String technicianId = document.getId();

        String name = safeValue(document.getString("name"));
        String email = safeValue(document.getString("email"));
        String phone = safeValue(document.getString("phone"));
        String branch = safeValue(document.getString("branch"));


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

        techniciansContainer.addView(card, cardParams);

        TextView title = new TextView(this);
        title.setText(name);
        title.setTextColor(getColor(R.color.tech_text));
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);

        card.addView(title);

        addDetail(card, "Email", email);
        addDetail(card, "Phone", phone);
        addDetail(card, "Branch", branch);


        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setPadding(0, dp(14), 0, 0);

        card.addView(buttonRow);

        Button editButton = new Button(this);
        editButton.setText("Edit");
        editButton.setAllCaps(false);
        editButton.setTextColor(getColor(R.color.tech_background));
        editButton.setBackgroundTintList(
                ColorStateList.valueOf(getColor(R.color.tech_teal))
        );

        LinearLayout.LayoutParams editParams =
                new LinearLayout.LayoutParams(0, dp(48), 1);

        buttonRow.addView(editButton, editParams);

        Button removeButton = new Button(this);
        removeButton.setText("Remove Role");
        removeButton.setAllCaps(false);
        removeButton.setTextColor(getColor(R.color.tech_text));
        removeButton.setBackgroundTintList(
                ColorStateList.valueOf(
                        getColor(R.color.tech_surface_light)
                )
        );

        LinearLayout.LayoutParams removeParams =
                new LinearLayout.LayoutParams(0, dp(48), 1);

        removeParams.leftMargin = dp(8);

        buttonRow.addView(removeButton, removeParams);

        editButton.setOnClickListener(v -> {

            editingTechnicianId = technicianId;

            txtTechnicianFormTitle.setText("Edit Technician");

            editTechnicianEmail.setText(email);
            editTechnicianEmail.setEnabled(false);

            editTechnicianName.setText(name);
            editTechnicianPhone.setText(phone);

            editTechnicianBranch.setText(
                    document.getString("branch")
            );


            btnSaveTechnician.setText("Save Changes");
            btnCancelTechnicianEdit.setVisibility(View.VISIBLE);

            editTechnicianName.requestFocus();
        });

        removeButton.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Remove technician role?")
                    .setMessage(
                            name + " will become a customer. " +
                                    "Their login account will remain active."
                    )
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Remove", (dialog, which) -> {

                        firestore.collection("users")
                                .document(technicianId)
                                .update("role", "customer")
                                .addOnSuccessListener(unused -> {

                                    if (technicianId.equals(
                                            editingTechnicianId
                                    )) {
                                        clearForm();
                                    }

                                    Toast.makeText(
                                            this,
                                            "Technician role removed",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    loadTechnicians();
                                })
                                .addOnFailureListener(e -> {

                                    showError(
                                            "Could not remove technician role",
                                            e
                                    );
                                });
                    })
                    .show();
        });
    }

    private void addDetail(
            LinearLayout card,
            String label,
            String value
    ) {

        TextView detail = new TextView(this);
        detail.setText(label + ": " + value);
        detail.setTextColor(getColor(R.color.tech_text_secondary));
        detail.setTextSize(14);
        detail.setPadding(0, dp(6), 0, 0);

        card.addView(detail);
    }

    private void clearForm() {

        editingTechnicianId = null;

        editTechnicianEmail.setText("");
        editTechnicianEmail.setEnabled(true);

        editTechnicianName.setText("");
        editTechnicianPhone.setText("");

        editTechnicianBranch.setText("");


        txtTechnicianFormTitle.setText(
                "Add Existing User as Technician"
        );

        btnSaveTechnician.setText("Assign Technician Role");
        btnCancelTechnicianEdit.setVisibility(View.GONE);
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

    private void showError(String message, Exception e) {

        Toast.makeText(
                this,
                message + ": " + e.getMessage(),
                Toast.LENGTH_LONG
        ).show();
    }
}
