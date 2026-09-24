
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

import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

    private String getCompressedImageBase64() throws IOException {

        Bitmap bitmap;

        if (cameraBitmap != null) {
            bitmap = cameraBitmap;

        } else if (imageUri != null) {

            // Read a smaller version of the selected gallery image.
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            try (InputStream input =
                         getContentResolver().openInputStream(imageUri)) {

                if (input == null) {
                    throw new IOException("Cannot open selected image");
                }

                BitmapFactory.decodeStream(input, null, options);
            }

            int sampleSize = 1;

            while (options.outWidth / sampleSize > 800
                    || options.outHeight / sampleSize > 800) {
                sampleSize *= 2;
            }

            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;

            try (InputStream input =
                         getContentResolver().openInputStream(imageUri)) {

                if (input == null) {
                    throw new IOException("Cannot read selected image");
                }

                bitmap = BitmapFactory.decodeStream(input, null, options);
            }

        } else {
            return null;
        }

        if (bitmap == null) {
            throw new IOException("Could not decode image");
        }

        // Limit the image dimensions before compression.
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scale = Math.min(
                1f,
                Math.min(800f / width, 800f / height)
        );

        Bitmap resized = bitmap;

        if (scale < 1f) {
            resized = Bitmap.createScaledBitmap(
                    bitmap,
                    Math.max(1, Math.round(width * scale)),
                    Math.max(1, Math.round(height * scale)),
                    true
            );
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // Reduce quality until the compressed image is at most 150 KB.
        int quality = 75;

        do {
            output.reset();
            resized.compress(Bitmap.CompressFormat.JPEG, quality, output);
            quality -= 10;

        } while (output.size() > 150 * 1024 && quality >= 25);

        byte[] imageBytes = output.toByteArray();

        if (imageBytes.length > 150 * 1024) {
            throw new IOException(
                    "Image is too large. Please choose another photo."
            );
        }

        return Base64.encodeToString(
                imageBytes,
                Base64.NO_WRAP
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

        if (imageUri == null && cameraBitmap == null) {
            Toast.makeText(
                    this,
                    "Please add a device photo",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        btnSubmitRepair.setEnabled(false);
        btnSubmitRepair.setText("Preparing photo...");

        // Process the photo away from the main UI thread.
        new Thread(() -> {

            String imageBase64;

            try {
                imageBase64 = getCompressedImageBase64();

            } catch (Exception e) {

                runOnUiThread(() -> {

                    btnSubmitRepair.setEnabled(true);
                    btnSubmitRepair.setText("Submit Repair Request");

                    Toast.makeText(
                            this,
                            "Photo error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });

                return;
            }

            runOnUiThread(() -> {

                if (firebaseAuth.getCurrentUser() == null) {

                    btnSubmitRepair.setEnabled(true);
                    btnSubmitRepair.setText("Submit Repair Request");

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

                repairData.put(
                        "deviceImageBase64",
                        imageBase64
                );

                btnSubmitRepair.setText("Submitting...");

                firestore.collection("appointments")
                        .add(repairData)
                        .addOnSuccessListener(documentReference -> {

                            documentReference.update(
                                    "appointmentId",
                                    documentReference.getId()
                            );

                            Toast.makeText(
                                    this,
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
                                    this,
                                    "Failed: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        });
            });

        }).start();
    }
}