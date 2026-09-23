package com.techfix.app.activities;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.techfix.app.R;

import java.util.Locale;

public class BranchesActivity extends AppCompatActivity {

    private Button btnFindNearest;
    private TextView txtNearestBranch;

    private FusedLocationProviderClient fusedLocationClient;

    private static final int LOCATION_PERMISSION_REQUEST = 100;

    // Approximate city-centre coordinates.
    // Replace these with actual TechFix branch coordinates when available.
    private static final double COLOMBO_LAT = 6.9271;
    private static final double COLOMBO_LNG = 79.8612;

    private static final double GALLE_LAT = 6.0329;
    private static final double GALLE_LNG = 80.2168;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_branches);

        btnFindNearest = findViewById(R.id.btnFindNearest);
        txtNearestBranch = findViewById(R.id.txtNearestBranch);

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        btnFindNearest.setOnClickListener(v -> findNearestBranch());
    }

    private void findNearestBranch() {

        boolean fineGranted = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        boolean coarseGranted = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        if (!fineGranted && !coarseGranted) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST
            );

            return;
        }

        txtNearestBranch.setText("Finding your nearest branch...");

        CancellationTokenSource tokenSource =
                new CancellationTokenSource();

        fusedLocationClient
                .getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        tokenSource.getToken()
                )
                .addOnSuccessListener(location -> {

                    if (location == null) {

                        txtNearestBranch.setText(
                                "Location unavailable. Turn on location and try again."
                        );

                        return;
                    }

                    calculateNearestBranch(location);
                })
                .addOnFailureListener(e -> {

                    txtNearestBranch.setText(
                            "Unable to find your location."
                    );

                    Toast.makeText(
                            this,
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

        boolean colomboIsNearest =
                colomboDistance[0] <= galleDistance[0];

        String branchName =
                colomboIsNearest ? "Colombo" : "Galle";

        double branchLat =
                colomboIsNearest ? COLOMBO_LAT : GALLE_LAT;

        double branchLng =
                colomboIsNearest ? COLOMBO_LNG : GALLE_LNG;

        float distanceKm =
                (colomboIsNearest
                        ? colomboDistance[0]
                        : galleDistance[0]) / 1000f;

        txtNearestBranch.setText(
                String.format(
                        Locale.getDefault(),
                        "Nearest branch: %s\nApproximate distance: %.1f km\nOpening Google Maps...",
                        branchName,
                        distanceKm
                )
        );

        openGoogleMaps(branchLat, branchLng);
    }

    private void openGoogleMaps(double latitude, double longitude) {

        Uri mapsUri = Uri.parse(
                "https://www.google.com/maps/dir/?api=1"
                        + "&destination=" + latitude + "," + longitude
                        + "&travelmode=driving"
        );

        Intent mapsIntent = new Intent(
                Intent.ACTION_VIEW,
                mapsUri
        );

        mapsIntent.setPackage("com.google.android.apps.maps");

        try {

            startActivity(mapsIntent);

        } catch (ActivityNotFoundException e) {

            // Open the same route in a browser if
            // the Google Maps app is not installed.

            Intent browserIntent = new Intent(
                    Intent.ACTION_VIEW,
                    mapsUri
            );

            try {
                startActivity(browserIntent);
            } catch (ActivityNotFoundException browserError) {

                Toast.makeText(
                        this,
                        "No maps application is available",
                        Toast.LENGTH_LONG
                ).show();
            }
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

            boolean granted = false;

            for (int result : grantResults) {

                if (result == PackageManager.PERMISSION_GRANTED) {
                    granted = true;
                    break;
                }
            }

            if (granted) {

                findNearestBranch();

            } else {

                txtNearestBranch.setText(
                        "Location permission is required to find the nearest branch."
                );
            }
        }
    }
}