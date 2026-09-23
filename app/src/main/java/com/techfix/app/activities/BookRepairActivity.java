
package com.techfix.app.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.techfix.app.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.widget.AdapterView;
import com.google.firebase.firestore.DocumentSnapshot;

public class BookRepairActivity extends AppCompatActivity {

    private Spinner spCategory;
    private Spinner spBranch;

    private EditText editBrand;
    private EditText editModel;
    private EditText editProblem;

    private TextView txtDate;
    private TextView txtImage;

    private TextView txtBranchAvailability;


    private Button btnSelectDate;
    private Button btnSelectImage;
    private Button btnSubmitRepair;

    private ImageView imagePreview;

    private Uri imageUri;
    private Bitmap cameraBitmap;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private ActivityResultLauncher<Void> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;

    private int availabilityRequestId = 0;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_book_repair);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        spCategory = findViewById(R.id.spCategory);
        spBranch = findViewById(R.id.spBranch);

        editBrand = findViewById(R.id.editBrand);
        editModel = findViewById(R.id.editModel);
        editProblem = findViewById(R.id.editProblem);

        txtDate = findViewById(R.id.txtDate);
        txtImage = findViewById(R.id.txtImage);

        txtBranchAvailability =
                findViewById(R.id.txtBranchAvailability);


        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSubmitRepair = findViewById(R.id.btnSubmitRepair);

        imagePreview = findViewById(R.id.imagePreview);

        setupSpinners();
        setupImageLaunchers();

        spBranch.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {
                        checkBranchAvailability();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        txtBranchAvailability.setText(
                                "Select a branch to check availability."
                        );
                    }
                }
        );

        spCategory.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {
                        checkBranchAvailability();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // No category selected.
                    }
                }
        );



        btnSelectDate.setOnClickListener(v -> showDatePicker());

        btnSelectImage.setOnClickListener(v -> selectImage());

        btnSubmitRepair.setOnClickListener(v -> submitRepair());
    }



    private void checkBranchAvailability() {

        // Each new selection gets its own request number.
        final int requestId = ++availabilityRequestId;

        if (spBranch.getSelectedItem() == null
                || spCategory.getSelectedItem() == null) {
            txtBranchAvailability.setText(
                    "Select a branch and device category."
            );
            return;
        }

        final String selectedBranch =
                spBranch.getSelectedItem().toString();

        final String selectedCategory =
                spCategory.getSelectedItem().toString();

        txtBranchAvailability.setText(
                "Checking availability at " + selectedBranch + "..."
        );

        firestore.collection("users")
                .whereEqualTo("role", "technician")
                .get()
                .addOnSuccessListener(technicianSnapshot -> {

                    // Ignore results from an older selection.
                    if (requestId != availabilityRequestId) {
                        return;
                    }

                    int technicianCount = 0;

                    for (com.google.firebase.firestore.DocumentSnapshot technician
                            : technicianSnapshot.getDocuments()) {

                        String branch = technician.getString("branch");

                        if (selectedBranch.equalsIgnoreCase(branch)) {
                            technicianCount++;
                        }
                    }

                    final int matchingTechnicians = technicianCount;

                    firestore.collection("spare_parts")
                            .get()
                            .addOnSuccessListener(partsSnapshot -> {

                                if (requestId != availabilityRequestId) {
                                    return;
                                }

                                int matchingParts = 0;

                                for (com.google.firebase.firestore.DocumentSnapshot part
                                        : partsSnapshot.getDocuments()) {

                                    String partBranch =
                                            part.getString("branch");

                                    String compatibleDevice =
                                            part.getString("device");

                                    Long quantity =
                                            part.getLong("quantity");

                                    if (selectedBranch.equalsIgnoreCase(partBranch)
                                            && selectedCategory.equalsIgnoreCase(
                                            compatibleDevice == null
                                                    ? "" : compatibleDevice
                                    )
                                            && quantity != null
                                            && quantity > 0) {
                                        matchingParts++;
                                    }
                                }

                                txtBranchAvailability.setText(
                                        selectedBranch + " branch\n"
                                                + "Technicians registered: "
                                                + matchingTechnicians + "\n"
                                                + "Compatible part types in stock: "
                                                + matchingParts + "\n\n"
                                                + "Availability is indicative. "
                                                + "The admin will confirm your repair."
                                );
                            })
                            .addOnFailureListener(e -> {
                                if (requestId == availabilityRequestId) {
                                    txtBranchAvailability.setText(
                                            "Could not check spare-parts stock. "
                                                    + "You can still submit a request."
                                    );
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    if (requestId == availabilityRequestId) {
                        txtBranchAvailability.setText(
                                "Could not check technician availability. "
                                        + "You can still submit a request."
                        );
                    }
                });
    }


    private void setupSpinners() {

        String[] deviceCategories = {
                "Mobile Phone",
                "Laptop",
                "Desktop Computer",
                "Tablet"
        };

        ArrayAdapter<String> categoryAdapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.item_tech_spinner,
                        deviceCategories
                );

        categoryAdapter.setDropDownViewResource(
                R.layout.item_tech_spinner_dropdown
        );

        spCategory.setAdapter(categoryAdapter);

        String[] branches = {
                "Colombo",
                "Galle"
        };

        ArrayAdapter<String> branchAdapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.item_tech_spinner,
                        branches
                );

        branchAdapter.setDropDownViewResource(
                R.layout.item_tech_spinner_dropdown
        );

        spBranch.setAdapter(branchAdapter);
    }

    private void setupImageLaunchers() {

        // Opens the camera and returns a preview-sized photo.
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicturePreview(),
                bitmap -> {

                    if (bitmap != null) {

                        cameraBitmap = bitmap;
                        imageUri = null;

                        imagePreview.setImageBitmap(bitmap);
                        imagePreview.setVisibility(View.VISIBLE);

                        txtImage.setText("Camera photo captured ✓");

                        Toast.makeText(
                                this,
                                "Photo captured successfully",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );

        // Opens the gallery / Android photo picker.
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {

                    if (uri != null) {

                        imageUri = uri;
                        cameraBitmap = null;

                        imagePreview.setImageURI(uri);
                        imagePreview.setVisibility(View.VISIBLE);

                        txtImage.setText("Gallery image selected ✓");

                        Toast.makeText(
                                this,
                                "Image selected successfully",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void selectImage() {

        String[] options = {
                "Take Photo with Camera",
                "Choose from Gallery"
        };

        new AlertDialog.Builder(this)
                .setTitle("Select Device Image")
                .setItems(options, (dialog, which) -> {

                    if (which == 0) {

                        cameraLauncher.launch(null);

                    } else {

                        galleryLauncher.launch("image/*");
                    }
                })
                .show();
    }

    private void showDatePicker() {

        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog =
                new DatePickerDialog(
                        this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {

                            String selectedDate =
                                    String.format(
                                            Locale.getDefault(),
                                            "%02d/%02d/%04d",
                                            selectedDay,
                                            selectedMonth + 1,
                                            selectedYear
                                    );

                            txtDate.setText(selectedDate);
                        },
                        year,
                        month,
                        day
                );

        datePickerDialog.show();

        datePickerDialog.getDatePicker().setMinDate(
                System.currentTimeMillis() - 1000
        );
    }

    private void submitRepair() {

        String brand = editBrand.getText().toString().trim();
        String model = editModel.getText().toString().trim();
        String problem = editProblem.getText().toString().trim();

        String category = spCategory.getSelectedItem().toString();
        String branch = spBranch.getSelectedItem().toString();

        String date = txtDate.getText().toString();

        if (brand.isEmpty()) {

            editBrand.setError("Enter device brand");
            editBrand.requestFocus();
            return;
        }

        if (model.isEmpty()) {

            editModel.setError("Enter device model");
            editModel.requestFocus();
            return;
        }

        if (problem.isEmpty()) {

            editProblem.setError("Describe the problem");
            editProblem.requestFocus();
            return;
        }

        if (date.equals("No date selected")) {

            Toast.makeText(
                    this,
                    "Please select a preferred date",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (firebaseAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login again",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String customerId =
                firebaseAuth.getCurrentUser().getUid();

        Map<String, Object> repairData = new HashMap<>();

        repairData.put("customerId", customerId);
        repairData.put("deviceCategory", category);
        repairData.put("brand", brand);
        repairData.put("model", model);
        repairData.put("problem", problem);
        repairData.put("preferredDate", date);
        repairData.put("branch", branch);

        repairData.put("status", "Pending");
        repairData.put("paymentStatus", "Unpaid");
        repairData.put("technicianId", "");

        repairData.put(
                "createdAt",
                System.currentTimeMillis()
        );

        // Prevent duplicate submissions while Firestore is saving.
        btnSubmitRepair.setEnabled(false);
        btnSubmitRepair.setText("Submitting...");

        firestore
                .collection("appointments")
                .add(repairData)

                .addOnSuccessListener(documentReference -> {

                    String appointmentId =
                            documentReference.getId();

                    documentReference.update(
                            "appointmentId",
                            appointmentId
                    );

                    Toast.makeText(
                            BookRepairActivity.this,
                            "Repair appointment submitted successfully",
                            Toast.LENGTH_LONG
                    ).show();

                    finish();
                })

                .addOnFailureListener(e -> {

                    btnSubmitRepair.setEnabled(true);
                    btnSubmitRepair.setText(
                            "Submit Repair Request"
                    );

                    Toast.makeText(
                            BookRepairActivity.this,
                            "Failed: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}
