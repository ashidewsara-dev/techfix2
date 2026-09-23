package com.techfix.app.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.techfix.app.R;

public class BranchesActivity extends AppCompatActivity {

    private Button btnFindNearest;
    private TextView txtNearestBranch;

    private FusedLocationProviderClient fusedLocationClient;

    private static final int LOCATION_PERMISSION_REQUEST = 100;

    // Approximate coordinates
    private final double COLOMBO_LAT = 6.9271;
    private final double COLOMBO_LNG = 79.8612;

    private final double GALLE_LAT = 6.0329;
    private final double GALLE_LNG = 80.2168;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_branches);

        btnFindNearest = findViewById(R.id.btnFindNearest);
        txtNearestBranch = findViewById(R.id.txtNearestBranch);

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        btnFindNearest.setOnClickListener(v -> findNearestBranch());
        findNearestBranch();
    }


    private void findNearestBranch() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST
            );

            return;
        }

        fusedLocationClient
                .getLastLocation()
                .addOnSuccessListener(location -> {

                    if (location == null) {

                        txtNearestBranch.setText(
                                "Location unavailable. Please turn on GPS and try again."
                        );

                        return;
                    }

                    calculateNearestBranch(location);
                })

                .addOnFailureListener(e -> {

                    Toast.makeText(
                            BranchesActivity.this,
                            "Location error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();

                });
    }


    private void calculateNearestBranch(Location userLocation) {

        float[] colomboDistance = new float[1];

        float[] galleDistance = new float[1];


        Location.distanceBetween(
                userLocation.getLatitude(),
                userLocation.getLongitude(),
                COLOMBO_LAT,
                COLOMBO_LNG,
                colomboDistance
        );


        Location.distanceBetween(
                userLocation.getLatitude(),
                userLocation.getLongitude(),
                GALLE_LAT,
                GALLE_LNG,
                galleDistance
        );


        float colomboKm =
                colomboDistance[0] / 1000;

        float galleKm =
                galleDistance[0] / 1000;


        if (colomboKm < galleKm) {

            txtNearestBranch.setText(
                    "Nearest Branch: Colombo\nDistance: "
                            + String.format("%.1f", colomboKm)
                            + " km"
            );

        } else {

            txtNearestBranch.setText(
                    "Nearest Branch: Galle\nDistance: "
                            + String.format("%.1f", galleKm)
                            + " km"
            );
        }
    }


    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );


        if (requestCode == LOCATION_PERMISSION_REQUEST) {

            if (grantResults.length > 0
                    && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {

                findNearestBranch();

            } else {

                Toast.makeText(
                        this,
                        "Location permission is required",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }
}