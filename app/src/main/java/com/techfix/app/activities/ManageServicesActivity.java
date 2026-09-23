
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
import java.util.Locale;
import java.util.Map;

public class ManageServicesActivity extends AppCompatActivity {

    private EditText editServiceName;
    private EditText editServicePrice;
    private EditText editServiceDescription;

    private TextView txtFormTitle;
    private TextView txtNoServices;

    private Button btnSaveService;
    private Button btnCancelEdit;
    private Button btnRefreshServices;

    private ProgressBar servicesProgressBar;
    private LinearLayout adminServicesContainer;

    private FirebaseFirestore firestore;

    // Null means we are adding a new service.
    private String editingServiceId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_manage_services);

        editServiceName = findViewById(R.id.editServiceName);
        editServicePrice = findViewById(R.id.editServicePrice);
        editServiceDescription =
                findViewById(R.id.editServiceDescription);

        txtFormTitle = findViewById(R.id.txtFormTitle);
        txtNoServices = findViewById(R.id.txtNoServices);

        btnSaveService = findViewById(R.id.btnSaveService);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);
        btnRefreshServices =
                findViewById(R.id.btnRefreshServices);

        servicesProgressBar =
                findViewById(R.id.servicesProgressBar);

        adminServicesContainer =
                findViewById(R.id.adminServicesContainer);

        firestore = FirebaseFirestore.getInstance();

        btnSaveService.setOnClickListener(v -> saveService());

        btnCancelEdit.setOnClickListener(v -> clearForm());

        btnRefreshServices.setOnClickListener(
                v -> loadServices()
        );

        loadServices();
    }

    private void saveService() {

        String name = editServiceName.getText()
                .toString().trim();

        String priceText = editServicePrice.getText()
                .toString().trim();

        String description = editServiceDescription.getText()
                .toString().trim();

        if (name.isEmpty()) {
            editServiceName.setError("Enter a service name");
            return;
        }

        if (priceText.isEmpty()) {
            editServicePrice.setError("Enter a price");
            return;
        }

        if (description.isEmpty()) {
            editServiceDescription.setError(
                    "Enter a description"
            );
            return;
        }

        double price;

        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            editServicePrice.setError("Enter a valid price");
            return;
        }

        if (!Double.isFinite(price) || price < 0) {
            editServicePrice.setError(
                    "Enter a valid non-negative price"
            );
            return;
        }

        Map<String, Object> service = new HashMap<>();
        service.put("name", name);
        service.put("price", price);
        service.put("description", description);

        btnSaveService.setEnabled(false);

        if (editingServiceId == null) {

            firestore.collection("services")
                    .add(service)
                    .addOnSuccessListener(reference -> {

                        btnSaveService.setEnabled(true);

                        Toast.makeText(
                                this,
                                "Service added",
                                Toast.LENGTH_SHORT
                        ).show();

                        clearForm();
                        loadServices();
                    })
                    .addOnFailureListener(e -> {

                        btnSaveService.setEnabled(true);
                        showError("Could not add service", e);
                    });

        } else {

            String serviceId = editingServiceId;

            firestore.collection("services")
                    .document(serviceId)
                    .update(service)
                    .addOnSuccessListener(unused -> {

                        btnSaveService.setEnabled(true);

                        Toast.makeText(
                                this,
                                "Service updated",
                                Toast.LENGTH_SHORT
                        ).show();

                        clearForm();
                        loadServices();
                    })
                    .addOnFailureListener(e -> {

                        btnSaveService.setEnabled(true);
                        showError("Could not update service", e);
                    });
        }
    }

    private void loadServices() {

        servicesProgressBar.setVisibility(View.VISIBLE);
        txtNoServices.setVisibility(View.GONE);
        btnRefreshServices.setEnabled(false);

        adminServicesContainer.removeAllViews();

        firestore.collection("services")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    servicesProgressBar.setVisibility(View.GONE);
                    btnRefreshServices.setEnabled(true);

                    if (querySnapshot.isEmpty()) {

                        txtNoServices.setText(
                                "No services added yet."
                        );

                        txtNoServices.setVisibility(View.VISIBLE);
                        return;
                    }

                    for (DocumentSnapshot document
                            : querySnapshot.getDocuments()) {

                        addServiceCard(document);
                    }
                })
                .addOnFailureListener(e -> {

                    servicesProgressBar.setVisibility(View.GONE);
                    btnRefreshServices.setEnabled(true);

                    txtNoServices.setText(
                            "Could not load services. Tap Refresh."
                    );

                    txtNoServices.setVisibility(View.VISIBLE);

                    showError("Could not load services", e);
                });
    }

    private void addServiceCard(DocumentSnapshot document) {

        String serviceId = document.getId();

        String name = safeValue(
                document.getString("name")
        );

        String description = safeValue(
                document.getString("description")
        );

        Double storedPrice = document.getDouble("price");
        double price = storedPrice == null ? 0 : storedPrice;

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

        adminServicesContainer.addView(card, cardParams);

        TextView title = new TextView(this);
        title.setText(name);
        title.setTextColor(getColor(R.color.tech_text));
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);

        card.addView(title);

        TextView priceView = new TextView(this);
        priceView.setText(
                String.format(
                        Locale.US,
                        "Starting from LKR %,.2f",
                        price
                )
        );

        priceView.setTextColor(getColor(R.color.tech_teal));
        priceView.setTextSize(15);
        priceView.setPadding(0, dp(8), 0, dp(8));

        card.addView(priceView);

        TextView descriptionView = new TextView(this);
        descriptionView.setText(description);
        descriptionView.setTextColor(
                getColor(R.color.tech_text_secondary)
        );
        descriptionView.setTextSize(14);

        card.addView(descriptionView);

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setPadding(0, dp(16), 0, 0);

        card.addView(buttonRow);

        Button editButton = new Button(this);
        editButton.setText("Edit");
        editButton.setAllCaps(false);
        editButton.setTextColor(
                getColor(R.color.tech_background)
        );
        editButton.setBackgroundTintList(
                ColorStateList.valueOf(
                        getColor(R.color.tech_teal)
                )
        );

        LinearLayout.LayoutParams editParams =
                new LinearLayout.LayoutParams(
                        0,
                        dp(48),
                        1
                );

        buttonRow.addView(editButton, editParams);

        Button deleteButton = new Button(this);
        deleteButton.setText("Delete");
        deleteButton.setAllCaps(false);
        deleteButton.setTextColor(
                getColor(R.color.tech_text)
        );
        deleteButton.setBackgroundTintList(
                ColorStateList.valueOf(
                        getColor(R.color.tech_surface_light)
                )
        );

        LinearLayout.LayoutParams deleteParams =
                new LinearLayout.LayoutParams(
                        0,
                        dp(48),
                        1
                );

        deleteParams.leftMargin = dp(8);

        buttonRow.addView(deleteButton, deleteParams);

        editButton.setOnClickListener(v -> {

            editingServiceId = serviceId;

            txtFormTitle.setText("Edit Service");
            btnSaveService.setText("Save Changes");
            btnCancelEdit.setVisibility(View.VISIBLE);

            editServiceName.setText(name);

            editServicePrice.setText(
                    String.valueOf(price)
            );

            editServiceDescription.setText(description);

            editServiceName.requestFocus();
        });

        deleteButton.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Delete service?")
                    .setMessage(
                            "Delete \"" + name + "\" permanently?"
                    )
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Delete", (dialog, which) -> {

                        firestore.collection("services")
                                .document(serviceId)
                                .delete()
                                .addOnSuccessListener(unused -> {

                                    if (serviceId.equals(editingServiceId)) {
                                        clearForm();
                                    }

                                    Toast.makeText(
                                            this,
                                            "Service deleted",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    loadServices();
                                })
                                .addOnFailureListener(e -> {
                                    showError(
                                            "Could not delete service",
                                            e
                                    );
                                });
                    })
                    .show();
        });
    }

    private void clearForm() {

        editingServiceId = null;

        editServiceName.setText("");
        editServicePrice.setText("");
        editServiceDescription.setText("");

        txtFormTitle.setText("Add New Service");
        btnSaveService.setText("Add Service");
        btnCancelEdit.setVisibility(View.GONE);
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
