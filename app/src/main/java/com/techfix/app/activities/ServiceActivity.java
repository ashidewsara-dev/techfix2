
package com.techfix.app.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.techfix.app.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ServiceActivity extends AppCompatActivity {

    private LinearLayout servicesContainer;
    private Button btnBookFromServices;
    private Button btnRefreshCustomerServices;
    private TextView txtServiceCount;
    private TextView txtNoServices;
    private ProgressBar servicesProgressBar;

    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_service);

        servicesContainer = findViewById(R.id.servicesContainer);
        btnBookFromServices = findViewById(R.id.btnBookFromServices);
        btnRefreshCustomerServices =
                findViewById(R.id.btnRefreshCustomerServices);

        txtServiceCount = findViewById(R.id.txtServiceCount);
        txtNoServices = findViewById(R.id.txtNoServices);
        servicesProgressBar = findViewById(R.id.servicesProgressBar);

        firestore = FirebaseFirestore.getInstance();

        btnBookFromServices.setOnClickListener(v -> {

            Intent intent = new Intent(
                    ServiceActivity.this,
                    BookRepairActivity.class
            );

            startActivity(intent);
        });

        btnRefreshCustomerServices.setOnClickListener(
                v -> loadServices()
        );

        loadServices();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reload when returning from another screen.
        if (firestore != null) {
            loadServices();
        }
    }

    private void loadServices() {

        servicesProgressBar.setVisibility(View.VISIBLE);
        txtNoServices.setVisibility(View.GONE);
        btnRefreshCustomerServices.setEnabled(false);

        txtServiceCount.setText("Loading services...");
        servicesContainer.removeAllViews();

        firestore.collection("services")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    servicesProgressBar.setVisibility(View.GONE);
                    btnRefreshCustomerServices.setEnabled(true);

                    List<DocumentSnapshot> services =
                            new ArrayList<>(querySnapshot.getDocuments());

                    services.sort(
                            Comparator.comparing(
                                    document -> {
                                        String name =
                                                document.getString("name");

                                        return name == null
                                                ? ""
                                                : name.toLowerCase(Locale.ROOT);
                                    }
                            )
                    );

                    int count = services.size();

                    txtServiceCount.setText(
                            count == 1
                                    ? "1 Repair Service Available"
                                    : count + " Repair Services Available"
                    );

                    if (count == 0) {

                        txtNoServices.setText(
                                "No repair services are available yet."
                        );

                        txtNoServices.setVisibility(View.VISIBLE);
                        return;
                    }

                    for (DocumentSnapshot document : services) {

                        String name = safeValue(
                                document.getString("name")
                        );

                        String description = safeValue(
                                document.getString("description")
                        );

                        Double price = document.getDouble("price");

                        String priceText;

                        if (price == null) {

                            priceText = "Contact us for pricing";

                        } else {

                            priceText = String.format(
                                    Locale.US,
                                    "Starting from LKR %,.2f",
                                    price
                            );
                        }

                        addServiceCard(
                                name,
                                priceText,
                                description
                        );
                    }
                })
                .addOnFailureListener(e -> {

                    servicesProgressBar.setVisibility(View.GONE);
                    btnRefreshCustomerServices.setEnabled(true);

                    txtServiceCount.setText(
                            "Services unavailable"
                    );

                    txtNoServices.setText(
                            "Could not load services.\n" +
                                    "Tap Refresh Services to try again."
                    );

                    txtNoServices.setVisibility(View.VISIBLE);

                    Toast.makeText(
                            this,
                            "Failed to load services: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void addServiceCard(
            String name,
            String price,
            String description
    ) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);

        card.setPadding(
                dp(18),
                dp(18),
                dp(18),
                dp(18)
        );

        card.setBackgroundResource(R.drawable.bg_tech_card);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin = dp(14);

        servicesContainer.addView(card, cardParams);

        TextView titleView = new TextView(this);
        titleView.setText(name);
        titleView.setTextColor(getColor(R.color.tech_text));
        titleView.setTextSize(18);
        titleView.setTypeface(null, Typeface.BOLD);

        card.addView(titleView);

        TextView descriptionView = new TextView(this);
        descriptionView.setText(description);
        descriptionView.setTextColor(
                getColor(R.color.tech_text_secondary)
        );
        descriptionView.setTextSize(14);
        descriptionView.setPadding(0, dp(8), 0, dp(12));

        card.addView(descriptionView);

        TextView priceView = new TextView(this);
        priceView.setText(price);
        priceView.setTextColor(getColor(R.color.tech_teal));
        priceView.setTextSize(15);
        priceView.setTypeface(null, Typeface.BOLD);

        card.addView(priceView);
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
}
