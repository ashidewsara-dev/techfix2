package com.techfix.app.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.techfix.app.R;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText editName;
    private EditText editEmail;
    private EditText editPhone;
    private EditText editPassword;
    private EditText editConfirmPassword;

    private Button btnRegister;
    private TextView txtLogin;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editRegisterEmail);
        editPhone = findViewById(R.id.editPhone);
        editPassword = findViewById(R.id.editRegisterPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        txtLogin = findViewById(R.id.txtLogin);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        txtLogin.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {

        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            editName.setError("Enter your name");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            editEmail.setError("Enter your email");
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            editPhone.setError("Enter your phone number");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editPassword.setError("Enter a password");
            return;
        }

        if (password.length() < 6) {
            editPassword.setError("Password must contain at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            editConfirmPassword.setError("Passwords do not match");
            return;
        }

        firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        String userId =
                                firebaseAuth.getCurrentUser().getUid();

                        Map<String, Object> user = new HashMap<>();

                        user.put("name", name);
                        user.put("email", email);
                        user.put("phone", phone);
                        user.put("role", "customer");

                        firestore
                                .collection("users")
                                .document(userId)
                                .set(user)
                                .addOnSuccessListener(unused -> {

                                    Toast.makeText(
                                            RegisterActivity.this,
                                            "Account created successfully",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    firebaseAuth.signOut();

                                    finish();
                                })
                                .addOnFailureListener(e -> {

                                    Toast.makeText(
                                            RegisterActivity.this,
                                            "Failed to save user: "
                                                    + e.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show();
                                });

                    } else {

                        String message = "Registration failed";

                        if (task.getException() != null) {
                            message += ": "
                                    + task.getException().getMessage();
                        }

                        Toast.makeText(
                                RegisterActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}