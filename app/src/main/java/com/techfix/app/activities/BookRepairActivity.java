package com.techfix.app.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.techfix.app.R;
import java.util.Calendar;
import java.util.Locale;
import android.content.Intent;
import android.net.Uri;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class BookRepairActivity extends AppCompatActivity {

    private Spinner spCategory;
    private Spinner spBranch;

    private EditText editBrand;
    private EditText editModel;
    private EditText editProblem;

    private TextView txtDate;
    private TextView txtImage;

    private Button btnSelectDate;
    private Button btnSelectImage;
    private Button btnSubmitRepair;
    private Uri imageUri;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private static final int IMAGE_REQUEST_CODE = 100;

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

        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSubmitRepair = findViewById(R.id.btnSubmitRepair);


        String[] deviceCategories = {
                "Mobile Phone",
                "Laptop",
                "Desktop Computer",
                "Tablet"
        };

        ArrayAdapter<String> categoryAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        deviceCategories
                );

        categoryAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spCategory.setAdapter(categoryAdapter);


        String[] branches = {
                "Colombo",
                "Galle"
        };

        ArrayAdapter<String> branchAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        branches
                );

        branchAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spBranch.setAdapter(branchAdapter);


        btnSelectDate.setOnClickListener(v -> showDatePicker());

        btnSelectImage.setOnClickListener(v -> selectImage());

        btnSubmitRepair.setOnClickListener(v -> submitRepair());
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

        datePickerDialog.getDatePicker()
                .setMinDate(System.currentTimeMillis());

        datePickerDialog.show();
    }


    private void selectImage() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        intent.setType("image/*");

        startActivityForResult(
                intent,
                IMAGE_REQUEST_CODE
        );
    }
    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == IMAGE_REQUEST_CODE
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            imageUri = data.getData();

            txtImage.setText("Device image selected ✓");

            Toast.makeText(
                    this,
                    "Image selected successfully",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }


    private void submitRepair() {

        String brand = editBrand.getText().toString().trim();
        String model = editModel.getText().toString().trim();
        String problem = editProblem.getText().toString().trim();

        String category = spCategory.getSelectedItem().toString();
        String branch = spBranch.getSelectedItem().toString();

        String date = txtDate.getText().toString();


        // Check brand
        if (brand.isEmpty()) {
            editBrand.setError("Enter device brand");
            return;
        }


        // Check model
        if (model.isEmpty()) {
            editModel.setError("Enter device model");
            return;
        }


        // Check problem
        if (problem.isEmpty()) {
            editProblem.setError("Describe the problem");
            return;
        }


        // Check date
        if (date.equals("No date selected")) {

            Toast.makeText(
                    this,
                    "Please select a preferred date",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // Check logged-in customer
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


        // Create repair appointment data
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


        // Save to Firestore
        firestore
                .collection("appointments")
                .add(repairData)

                .addOnSuccessListener(documentReference -> {

                    String appointmentId =
                            documentReference.getId();

                    // Save appointment ID inside the document
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

                    Toast.makeText(
                            BookRepairActivity.this,
                            "Failed: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                });
    }
}