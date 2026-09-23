
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
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.techfix.app.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.Locale;

public class BranchesActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 100;

    // Approximate city-centre coordinates.
    // Replace with the actual TechFix branch coordinates.
    private static final GeoPoint COLOMBO =
            new GeoPoint(6.9271, 79.8612);

    private static final GeoPoint GALLE =
            new GeoPoint(6.0329, 80.2168);

    private MapView branchMap;
    private TextView txtNearestBranch;
    private Button btnFindNearest;

    private FusedLocationProviderClient fusedLocationClient;

    private Marker customerMarker;
    private boolean findingLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Identify the app to the OpenStreetMap tile server.
        Configuration.getInstance().setUserAgentValue(
                getPackageName()
        );

        setContentView(R.layout.activity_branches);

        branchMap = findViewById(R.id.branchMap);
        txtNearestBranch = findViewById(R.id.txtNearestBranch);
        btnFindNearest = findViewById(R.id.btnFindNearest);

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        setupMap();

        btnFindNearest.setOnClickListener(
                v -> findNearestBranch()
        );
    }

    private void setupMap() {

        branchMap.setTileSource(TileSourceFactory.MAPNIK);
        branchMap.setMultiTouchControls(true);

        branchMap.getController().setZoom(8.0);
        branchMap.getController().setCenter(
                new GeoPoint(6.5, 80.0)
        );

        addBranchMarker(
                COLOMBO,
                "TechFix Colombo"
        );

        addBranchMarker(
                GALLE,
                "TechFix Galle"
        );
    }

    private void addBranchMarker(
            GeoPoint position,
            String title
    ) {

        Marker marker = new Marker(branchMap);

        marker.setPosition(position);
        marker.setTitle(title);
        marker.setAnchor(
                Marker.ANCHOR_CENTER,
                Marker.ANCHOR_BOTTOM
        );

        branchMap.getOverlays().add(marker);
        branchMap.invalidate();
    }

    private void findNearestBranch() {

        if (findingLocation) {
            return;
        }

        boolean fineGranted =
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        boolean coarseGranted =
                ActivityCompat.checkSelfPermission(
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

        findingLocation = true;
        btnFindNearest.setEnabled(false);

        txtNearestBranch.setText(
                "Finding your current location..."
        );

        CancellationTokenSource tokenSource =
                new CancellationTokenSource();

        fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        tokenSource.getToken()
                )
                .addOnSuccessListener(location -> {

                    findingLocation = false;
                    btnFindNearest.setEnabled(true);

                    if (location == null) {

                        txtNearestBranch.setText(
                                "Location unavailable. " +
                                        "Turn on your phone's Location and try again."
                        );

                        return;
                    }

                    showNearestBranch(location);
                })
                .addOnFailureListener(e -> {

                    findingLocation = false;
                    btnFindNearest.setEnabled(true);

                    txtNearestBranch.setText(
                            "Unable to detect your location. Please try again."
                    );

                    Toast.makeText(
                            this,
                            "Location error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void showNearestBranch(Location location) {

        float[] colomboDistance = new float[1];
        float[] galleDistance = new float[1];

        Location.distanceBetween(
                location.getLatitude(),
                location.getLongitude(),
                COLOMBO.getLatitude(),
                COLOMBO.getLongitude(),
                colomboDistance
        );

        Location.distanceBetween(
                location.getLatitude(),
                location.getLongitude(),
                GALLE.getLatitude(),
                GALLE.getLongitude(),
                galleDistance
        );

        boolean colomboIsNearest =
                colomboDistance[0] <= galleDistance[0];

        String nearestName =
                colomboIsNearest ? "Colombo" : "Galle";

        GeoPoint nearestPosition =
                colomboIsNearest ? COLOMBO : GALLE;

        float distanceKm =
                (colomboIsNearest
                        ? colomboDistance[0]
                        : galleDistance[0]) / 1000f;

        txtNearestBranch.setText(
                String.format(
                        Locale.getDefault(),
                        "Nearest branch: %s • Approx. %.1f km away",
                        nearestName,
                        distanceKm
                )
        );

        // Remove the previous customer marker if the button
        // is pressed again.
        if (customerMarker != null) {
            branchMap.getOverlays().remove(customerMarker);
        }

        customerMarker = new Marker(branchMap);

        customerMarker.setPosition(
                new GeoPoint(
                        location.getLatitude(),
                        location.getLongitude()
                )
        );

        customerMarker.setTitle("Your Location");

        customerMarker.setAnchor(
                Marker.ANCHOR_CENTER,
                Marker.ANCHOR_BOTTOM
        );

        branchMap.getOverlays().add(customerMarker);

        // Zoom to the nearest branch without leaving TechFix.
        branchMap.getController().animateTo(nearestPosition);
        branchMap.getController().setZoom(14.0);

        branchMap.invalidate();
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
                        "Location permission is required " +
                                "to find your nearest branch."
                );
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (branchMap != null) {
            branchMap.onResume();
        }
    }

    @Override
    protected void onPause() {

        if (branchMap != null) {
            branchMap.onPause();
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        if (branchMap != null) {
            branchMap.onDetach();
        }

        super.onDestroy();
    }
}
