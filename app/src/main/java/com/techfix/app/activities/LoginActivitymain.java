package com.techfix.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.techfix.app.R;

public class LoginActivitymain extends AppCompatActivity {

    private EditText editEmail;
    private EditText editPassword;

    private Button btnLogin;

    private TextView txtRegister;
    private TextView txtForgotPassword;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);

        btnLogin = findViewById(R.id.btnLogin);

        txtRegister = findViewById(R.id.txtRegister);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();


        // LOGIN
        btnLogin.setOnClickListener(v -> {

            String email =
                    editEmail.getText().toString().trim();

            String password =
                    editPassword.getText().toString().trim();


            if (email.isEmpty()) {

                editEmail.setError("Enter email");
                return;
            }


            if (password.isEmpty()) {

                editPassword.setError("Enter password");
                return;
            }


            firebaseAuth
                    .signInWithEmailAndPassword(email, password)

                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            if (firebaseAuth.getCurrentUser() == null) {

                                Toast.makeText(
                                        LoginActivitymain.this,
                                        "Login failed",
                                        Toast.LENGTH_LONG
                                ).show();

                                return;
                            }

                            String userId =
                                    firebaseAuth
                                            .getCurrentUser()
                                            .getUid();


                            firestore
                                    .collection("users")
                                    .document(userId)
                                    .get()

                                    .addOnSuccessListener(documentSnapshot -> {

                                        if (!documentSnapshot.exists()) {

                                            Toast.makeText(
                                                    LoginActivitymain.this,
                                                    "User profile not found",
                                                    Toast.LENGTH_LONG
                                            ).show();

                                            return;
                                        }


                                        String role =
                                                documentSnapshot
                                                        .getString("role");

                                        Toast.makeText(
                                                LoginActivitymain.this,
                                                "Logged-in UID: " + userId + "\nFirestore role: " + role,
                                                Toast.LENGTH_LONG
                                        ).show();


                                        if ("admin".equalsIgnoreCase(role)) {

                                            Intent intent =
                                                    new Intent(
                                                            LoginActivitymain.this,
                                                            AdminDashboardActivity.class
                                                    );

                                            startActivity(intent);
                                            finish();


                                        } else if ("technician"
                                                .equalsIgnoreCase(role)) {

                                            Intent intent =
                                                    new Intent(
                                                            LoginActivitymain.this,
                                                            TechnicianDashboardActivity.class
                                                    );

                                            startActivity(intent);
                                            finish();


                                        } else {

                                            Intent intent =
                                                    new Intent(
                                                            LoginActivitymain.this,
                                                            CustomerDashboardActivity.class
                                                    );

                                            startActivity(intent);
                                            finish();
                                        }

                                    })

                                    .addOnFailureListener(e -> {

                                        Toast.makeText(
                                                LoginActivitymain.this,
                                                "Failed to load role: "
                                                        + e.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show();

                                    });


                        } else {

                            String errorMessage = "Login failed";

                            if (task.getException() != null) {

                                errorMessage =
                                        task
                                                .getException()
                                                .getMessage();
                            }

                            Toast.makeText(
                                    LoginActivitymain.this,
                                    errorMessage,
                                    Toast.LENGTH_LONG
                            ).show();
                        }

                    });
        });


        // OPEN REGISTER PAGE
        txtRegister.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            LoginActivitymain.this,
                            RegisterActivity.class
                    );

            startActivity(intent);
        });


        // FORGOT PASSWORD
        txtForgotPassword.setOnClickListener(v -> {

            String email =
                    editEmail.getText().toString().trim();


            if (email.isEmpty()) {

                editEmail.setError(
                        "Enter your email first"
                );

                return;
            }


            firebaseAuth
                    .sendPasswordResetEmail(email)

                    .addOnSuccessListener(unused -> {

                        Toast.makeText(
                                LoginActivitymain.this,
                                "Password reset email sent",
                                Toast.LENGTH_LONG
                        ).show();

                    })

                    .addOnFailureListener(e -> {

                        Toast.makeText(
                                LoginActivitymain.this,
                                e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();

                    });
        });
    }
}