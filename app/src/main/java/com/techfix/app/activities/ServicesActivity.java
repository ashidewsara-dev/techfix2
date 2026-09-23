package com.techfix.app.activities;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.techfix.app.R;

public class ServicesActivity extends AppCompatActivity {

    private LinearLayout servicesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_services);

        servicesContainer = findViewById(R.id.servicesContainer);

        loadServices();
    }

    private void loadServices() {

        String[] services = {

                "Screen Replacement\nStarting from LKR 8,000",

                "Battery Replacement\nStarting from LKR 5,000",

                "Charging Port Repair\nStarting from LKR 4,500",

                "Laptop Keyboard Replacement\nStarting from LKR 7,500",

                "Software / Operating System Repair\nStarting from LKR 3,500",

                "Device Cleaning and Diagnosis\nStarting from LKR 2,000"
        };

        for (String service : services) {

            TextView serviceView =
                    new TextView(this);

            serviceView.setText(service);

            serviceView.setTextSize(17);

            serviceView.setPadding(
                    30,
                    30,
                    30,
                    30
            );

            servicesContainer.addView(
                    serviceView
            );
        }
    }
}