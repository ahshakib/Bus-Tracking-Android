package com.example.trackcoubus_student;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class StudentsMapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;

    private Button mFinder;
    private LatLng myLocation, findLocation;

    private String uId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_students_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        Button mLogout = (Button) findViewById(R.id.slogout);
        mFinder = (Button) findViewById(R.id.finder);

        mLogout.setOnClickListener(view -> {
            Intent intent = new Intent(StudentsMapsActivity.this, SelectionPageActivity.class);
            startActivity(intent);
            finish();
        });

        mFinder.setOnClickListener(view -> {
            String user_id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            uId = user_id;
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Request");
            GeoFire geofire = new GeoFire(ref);
            geofire.setLocation(user_id, new GeoLocation( mLastLocation.getLatitude(), mLastLocation.getLongitude()));
            myLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(myLocation).title("You").icon(BitmapDescriptorFactory.defaultMarker(270.0f)));
            mFinder.setText("Finding Bus...");

            getBuses();
        });
    }
    private int radius = 1;
    private Boolean driverFound = false;
    private String driverID;
    Marker marker;
    void getBuses(){
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("Users").child("Vehicle").child("Bus-1");

        GeoFire geofire = new GeoFire(driverLocation);
        GeoQuery geoquery = geofire.queryAtLocation(new GeoLocation(myLocation.latitude, myLocation.longitude), radius);
        geoquery.removeAllListeners();

        geoquery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!driverFound){
                    driverFound = true;
                    driverID = key;
                }
            }

            @Override
            public void onKeyExited(String key) {
                System.out.println("Key: "+key);
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if(!driverFound){
                    radius++;
                    if(radius>8587){
                        Toast.makeText(StudentsMapsActivity.this, "No Location Found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    getBuses();
                }
                else{
                    DatabaseReference show = FirebaseDatabase.getInstance().getReference().child("Users").child("Vehicle").child("Bus-1");
                    GeoFire geofire = new GeoFire(show);
                    geofire.getLocation(driverID, new LocationCallback() {
                        @Override
                        public void onLocationResult(String key, GeoLocation location) {
                            if(location != null){
                                if(marker != null){
                                    marker.remove();
                                }
                                marker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)).title("Bus"));
                                getBuses();
                            }
                            else{
                                Toast.makeText(StudentsMapsActivity.this, "No Location Found", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        mLastLocation = location;
        LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        String user_id = uId;
        Toast.makeText(StudentsMapsActivity.this, "Bus-1", Toast.LENGTH_SHORT).show();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Request");
        GeoFire geofire = new GeoFire(ref);
        geofire.removeLocation(user_id);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}