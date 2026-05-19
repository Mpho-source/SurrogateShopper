package com.example.surrogateshopper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class navigate extends AppCompatActivity {

    private MapView mapView;
    private Button btnStatusAction;
    private TextView tvDeliveryAddress;

    private String requestId;
    private String currentStatus = "Out for Delivery";
    private double destLat, destLng;
    private String destAddress;

    private MyLocationNewOverlay mLocationOverlay;
    private Polyline routePolyline;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_navigate);

        requestId = getIntent().getStringExtra("REQUEST_ID");
        destLat = getIntent().getDoubleExtra("LATITUDE", -26.1929);
        destLng = getIntent().getDoubleExtra("LONGITUDE", 28.0305);
        destAddress = getIntent().getStringExtra("ADDRESS");

        mapView = findViewById(R.id.mapView);
        btnStatusAction = findViewById(R.id.btnStatusAction);
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);

        if (destAddress != null) {
            tvDeliveryAddress.setText(destAddress);
        }

        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(16.5);
        GeoPoint targetPoint = new GeoPoint(destLat, destLng);
        mapView.getController().setCenter(targetPoint);

        Marker nodeMarker = new Marker(mapView);
        nodeMarker.setPosition(targetPoint);
        nodeMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        nodeMarker.setTitle("Delivery Destination Target");
        mapView.getOverlays().add(nodeMarker);

        // Initial text display setup
        btnStatusAction.setText("ARRIVED");

        // Click logic registered exactly ONCE here to prevent recursive event stack accumulation
        btnStatusAction.setOnClickListener(v -> {
            if (currentStatus.equals("Out for Delivery")) {
                updateDatabaseStatus("Arrived");
            } else if (currentStatus.equals("Arrived")) {
                updateDatabaseStatus("Completed");
            }
        });

        checkLocationPermissions();
    }

    private void checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            enableLocation();
        }
    }

    private void enableLocation() {
        mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        mLocationOverlay.enableMyLocation();
        mLocationOverlay.enableFollowLocation();

        mLocationOverlay.runOnFirstFix(() -> runOnUiThread(() -> {
            GeoPoint currentDeviceLocation = mLocationOverlay.getMyLocation();
            if (currentDeviceLocation != null) {
                getRouteFromOSRM(currentDeviceLocation.getLatitude(), currentDeviceLocation.getLongitude(), destLat, destLng);
            }
        }));

        mapView.getOverlays().add(mLocationOverlay);
    }

    private void getRouteFromOSRM(double startLat, double startLng, double endLat, double endLng) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://router.project-osrm.org/route/v1/driving/"
                + startLng + "," + startLat + ";" + endLng + "," + endLat
                + "?overview=full&geometries=geojson";

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(navigate.this, "Routing path connection error.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        JSONArray routes = jsonResponse.getJSONArray("routes");
                        if (routes.length() > 0) {
                            JSONObject route = routes.getJSONObject(0);
                            JSONObject geometry = route.getJSONObject("geometry");
                            JSONArray coordinates = geometry.getJSONArray("coordinates");

                            List<GeoPoint> routePoints = new ArrayList<>();
                            for (int i = 0; i < coordinates.length(); i++) {
                                JSONArray coordArray = coordinates.getJSONArray(i);
                                routePoints.add(new GeoPoint(coordArray.getDouble(1), coordArray.getDouble(0)));
                            }
                            runOnUiThread(() -> drawRouteOnMap(routePoints));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void drawRouteOnMap(List<GeoPoint> points) {
        if (routePolyline != null) {
            mapView.getOverlays().remove(routePolyline);
        }
        routePolyline = new Polyline();
        routePolyline.setPoints(points);
        routePolyline.setColor(Color.parseColor("#1A73E8"));
        routePolyline.setWidth(12.0f);

        mapView.getOverlays().add(routePolyline);
        mapView.invalidate();
    }

    private void updateDatabaseStatus(String targetStatus) {
        OkHttpClient client = new OkHttpClient();
        RequestBody formBody = new FormBody.Builder()
                .add("request_id", requestId)
                .add("status", targetStatus)
                .build();

        Request request = new Request.Builder()
                .url("https://wmc.ms.wits.ac.za/students/sgroup2715/updateStatus.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(navigate.this, "Network failure changing status.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String resultText = response.body().string().trim();
                    try {
                        // Safe parsing using explicit JSON parsing engine maps
                        JSONObject jsonObject = new JSONObject(resultText);
                        String statusResponse = jsonObject.getString("status");

                        if (statusResponse.equals("success")) {
                            runOnUiThread(() -> {
                                currentStatus = targetStatus; // Safely update tracking state

                                if (currentStatus.equals("Arrived")) {
                                    Toast.makeText(navigate.this, "Status updated: Arrived!", Toast.LENGTH_SHORT).show();
                                    btnStatusAction.setText("COMPLETE ORDER"); // Change visual presentation text instantly
                                } else if (currentStatus.equals("Completed")) {
                                    Toast.makeText(navigate.this, "Order Delivery Complete!", Toast.LENGTH_LONG).show();
                                    finish(); // Tear down navigation view safely
                                }
                            });
                        } else {
                            String errorMsg = jsonObject.optString("message", "Unknown error");
                            runOnUiThread(() -> Toast.makeText(navigate.this, "Server Error: " + errorMsg, Toast.LENGTH_LONG).show());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(navigate.this, "Parsing Error: Check backend output format.", Toast.LENGTH_LONG).show());
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableLocation();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if (mLocationOverlay != null) mLocationOverlay.enableMyLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (mLocationOverlay != null) mLocationOverlay.disableMyLocation();
    }
}