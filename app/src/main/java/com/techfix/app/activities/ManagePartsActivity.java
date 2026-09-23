
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

public class ManagePartsActivity extends AppCompatActivity {

    private EditText editPartName;
    private EditText editPartDevice;
    private EditText editPartPrice;
    private EditText editPartQuantity;

    private EditText editPartBranch;

    private TextView txtPartFormTitle;
    private TextView txtPartCount;
    private TextView txtNoParts;

    private Button btnSavePart;
    private Button btnCancelPartEdit;
    private Button btnRefreshParts;

    private ProgressBar partsProgressBar;
    private LinearLayout partsContainer;

    private FirebaseFirestore firestore;

    // Null means the form is adding a new part.
    private String editingPartId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_manage_parts);

        editPartName = findViewById(R.id.editPartName);
        editPartDevice = findViewById(R.id.editPartDevice);
        editPartPrice = findViewById(R.id.editPartPrice);
        editPartQuantity = findViewById(R.id.editPartQuantity);

        editPartBranch = findViewById(R.id.editPartBranch);


        txtPartFormTitle = findViewById(R.id.txtPartFormTitle);
        txtPartCount = findViewById(R.id.txtPartCount);
        txtNoParts = findViewById(R.id.txtNoParts);

        btnSavePart = findViewById(R.id.btnSavePart);
        btnCancelPartEdit = findViewById(R.id.btnCancelPartEdit);
        btnRefreshParts = findViewById(R.id.btnRefreshParts);

        partsProgressBar = findViewById(R.id.partsProgressBar);
        partsContainer = findViewById(R.id.partsContainer);

        firestore = FirebaseFirestore.getInstance();

        btnSavePart.setOnClickListener(v -> savePart());
        btnCancelPartEdit.setOnClickListener(v -> clearForm());
        btnRefreshParts.setOnClickListener(v -> loadParts());

        loadParts();
    }

    private void savePart() {

        String name = editPartName.getText()
                .toString().trim();

        String device = editPartDevice.getText()
                .toString().trim();


        String branch = editPartBranch.getText()
                .toString().trim();

        if (!branch.equalsIgnoreCase("Colombo")
                && !branch.equalsIgnoreCase("Galle")) {
            editPartBranch.setError("Enter Colombo or Galle");
            editPartBranch.requestFocus();
            return;
        }

        branch = branch.equalsIgnoreCase("Colombo")
                ? "Colombo" : "Galle";

        String priceText = editPartPrice.getText()
                .toString().trim();

        String quantityText = editPartQuantity.getText()
                .toString().trim();

        if (name.isEmpty()) {
            editPartName.setError("Enter a part name");
            return;
        }

        if (device.isEmpty()) {
            editPartDevice.setError("Enter a compatible device");
            return;
        }

        if (priceText.isEmpty()) {
            editPartPrice.setError("Enter a unit price");
            return;
        }

        if (quantityText.isEmpty()) {
            editPartQuantity.setError("Enter a stock quantity");
            return;
        }

        double price;
        long quantity;

        try {
            price = Double.parseDouble(priceText);
            quantity = Long.parseLong(quantityText);
        } catch (NumberFormatException e) {

            Toast.makeText(
                    this,
                    "Enter a valid price and quantity",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (!Double.isFinite(price) || price < 0) {
            editPartPrice.setError(
                    "Enter a valid non-negative price"
            );
            return;
        }

        if (quantity < 0) {
            editPartQuantity.setError(
                    "Quantity cannot be negative"
            );
            return;
        }

        Map<String, Object> part = new HashMap<>();
        part.put("name", name);
        part.put("device", device);
        part.put("price", price);
        part.put("quantity", quantity);

        part.put("branch", branch);


        btnSavePart.setEnabled(false);

        if (editingPartId == null) {

            firestore.collection("spare_parts")
                    .add(part)
                    .addOnSuccessListener(reference -> {

                        btnSavePart.setEnabled(true);

                        Toast.makeText(
                                this,
                                "Spare part added",
                                Toast.LENGTH_SHORT
                        ).show();

                        clearForm();
                        loadParts();
                    })
                    .addOnFailureListener(e -> {

                        btnSavePart.setEnabled(true);
                        showError("Could not add part", e);
                    });

        } else {

            String partId = editingPartId;

            firestore.collection("spare_parts")
                    .document(partId)
                    .update(part)
                    .addOnSuccessListener(unused -> {

                        btnSavePart.setEnabled(true);

                        Toast.makeText(
                                this,
                                "Spare part updated",
                                Toast.LENGTH_SHORT
                        ).show();

                        clearForm();
                        loadParts();
                    })
                    .addOnFailureListener(e -> {

                        btnSavePart.setEnabled(true);
                        showError("Could not update part", e);
                    });
        }
    }

    private void loadParts() {

        partsProgressBar.setVisibility(View.VISIBLE);
        btnRefreshParts.setEnabled(false);
        txtNoParts.setVisibility(View.GONE);

        partsContainer.removeAllViews();

        firestore.collection("spare_parts")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    partsProgressBar.setVisibility(View.GONE);
                    btnRefreshParts.setEnabled(true);

                    int count = querySnapshot.size();

                    txtPartCount.setText(
                            count == 1
                                    ? "1 Spare Part"
                                    : count + " Spare Parts"
                    );

                    if (count == 0) {

                        txtNoParts.setText(
                                "No spare parts added yet."
                        );

                        txtNoParts.setVisibility(View.VISIBLE);
                        return;
                    }

                    for (DocumentSnapshot document
                            : querySnapshot.getDocuments()) {

                        addPartCard(document);
                    }
                })
                .addOnFailureListener(e -> {

                    partsProgressBar.setVisibility(View.GONE);
                    btnRefreshParts.setEnabled(true);

                    txtPartCount.setText(
                            "Inventory unavailable"
                    );

                    txtNoParts.setText(
                            "Could not load inventory. Tap Refresh."
                    );

                    txtNoParts.setVisibility(View.VISIBLE);

                    showError("Could not load inventory", e);
                });
    }

    private void addPartCard(DocumentSnapshot document) {

        String partId = document.getId();

        String name = safeValue(
                document.getString("name")
        );

        String device = safeValue(
                document.getString("device")
        );

        String branch = safeValue(
                document.getString("branch")
        );


        Double storedPrice = document.getDouble("price");
        Long storedQuantity = document.getLong("quantity");

        double price = storedPrice == null ? 0 : storedPrice;
        long quantity = storedQuantity == null
                ? 0L : storedQuantity;

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

        partsContainer.addView(card, cardParams);

        TextView title = new TextView(this);
        title.setText(name);
        title.setTextColor(getColor(R.color.tech_text));
        title.setTextSize(18);
        title.setTypeface(null, Typeface.BOLD);

        card.addView(title);

        addDetail(card, "Compatible device", device);

        addDetail(card, "Branch", branch);


        TextView priceView = new TextView(this);
        priceView.setText(
                String.format(
                        Locale.US,
                        "Unit price: LKR %,.2f",
                        price
                )
        );

        priceView.setTextColor(getColor(R.color.tech_teal));
        priceView.setTextSize(15);
        priceView.setPadding(0, dp(10), 0, dp(6));

        card.addView(priceView);

        TextView stockView = new TextView(this);

        if (quantity == 0) {

            stockView.setText("OUT OF STOCK");
            stockView.setTextColor(
                    getColor(R.color.tech_warning)
            );

        } else if (quantity <= 5) {

            stockView.setText(
                    "LOW STOCK: " + quantity + " remaining"
            );

            stockView.setTextColor(
                    getColor(R.color.tech_warning)
            );

        } else {

            stockView.setText(
                    "IN STOCK: " + quantity + " available"
            );

            stockView.setTextColor(
                    getColor(R.color.tech_teal)
            );
        }

        stockView.setTextSize(14);
        stockView.setTypeface(null, Typeface.BOLD);

        card.addView(stockView);

        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setPadding(0, dp(14), 0, 0);

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

            editingPartId = partId;

            txtPartFormTitle.setText("Edit Spare Part");
            btnSavePart.setText("Save Changes");
            btnCancelPartEdit.setVisibility(View.VISIBLE);

            editPartName.setText(name);
            editPartDevice.setText(device);

            editPartBranch.setText(
                    document.getString("branch")
            );

            editPartPrice.setText(String.valueOf(price));
            editPartQuantity.setText(String.valueOf(quantity));

            editPartName.requestFocus();
        });

        deleteButton.setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Delete spare part?")
                    .setMessage(
                            "Delete \"" + name + "\" permanently?"
                    )
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton(
                            "Delete",
                            (dialog, which) -> {

                                firestore.collection("spare_parts")
                                        .document(partId)
                                        .delete()
                                        .addOnSuccessListener(unused -> {

                                            if (partId.equals(editingPartId)) {
                                                clearForm();
                                            }

                                            Toast.makeText(
                                                    this,
                                                    "Spare part deleted",
                                                    Toast.LENGTH_SHORT
                                            ).show();

                                            loadParts();
                                        })
                                        .addOnFailureListener(e -> {

                                            showError(
                                                    "Could not delete part",
                                                    e
                                            );
                                        });
                            }
                    )
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
        detail.setTextColor(
                getColor(R.color.tech_text_secondary)
        );
        detail.setTextSize(14);
        detail.setPadding(0, dp(6), 0, 0);

        card.addView(detail);
    }

    private void clearForm() {

        editingPartId = null;

        editPartName.setText("");
        editPartDevice.setText("");
        editPartPrice.setText("");
        editPartQuantity.setText("");

        editPartBranch.setText("");


        txtPartFormTitle.setText("Add New Part");
        btnSavePart.setText("Add Part");
        btnCancelPartEdit.setVisibility(View.GONE);
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
