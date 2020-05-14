package com.example.diushuttle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.internal.GoogleApiAvailabilityCache;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;

public class Rider extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, NavigationView.OnNavigationItemSelectedListener , com.google.android.gms.location.LocationListener {

    //Declare all the layout and View
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;

    TextView headername, headeremail , showspeed, showlocation;
    Button mRequest;
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClint;
    Location lastlocation;
    LocationRequest mLocationRequest;
    LocationManager locationManager;
    LocationListener locationListener;

    private LatLng pickupLocation;
    ProgressDialog progressDialog;
    boolean isLogout = false;
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if( requestCode == 1 ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                }
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setupLayout();
        startProgress();
        mRequest = findViewById(R.id.send_request);
        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Store Rider Last Location.
                if( !isLogout) {
                    String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("riderAvailable");
                    GeoFire geoFire = new GeoFire(ref);
                    geoFire.setLocation(userid, new GeoLocation(lastlocation.getLatitude(), lastlocation.getLongitude()));
                    pickupLocation = new LatLng(lastlocation.getLatitude(), lastlocation.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Here"));
                    mRequest.setText("Please wait...");
                    getClosestBus();
                }
            }
        });
    }
    private int radius = 1;
    private boolean driverFound = false;
    private String driverAvailableID;
    private void getClosestBus(){
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driverAvailable");
        GeoFire geoFire = new GeoFire(driverLocation);
        GeoQuery geoQuery = geoFire.queryAtLocation( new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius );
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if( !driverFound ) {
                    driverFound = true;
                    driverAvailableID = key;
                    String customerId = FirebaseAuth.getInstance().getUid();
                    DatabaseReference driverref = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver")
                            .child(driverAvailableID)
                            .child("customerRiderId").child(customerId);

                    driverref.setValue(true);
                    //HashMap hashMap = new HashMap();
                    //hashMap.put("customerRiderId" , customerId );
                    //driverref.updateChildren( hashMap );
                    getDriverLocation();
                    mRequest.setText("Looking for bus location");
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if( !driverFound ){
                    radius++;
                    if( radius < 101 ) {
                        getClosestBus();
                    }
                    else{
                        radius = 1;
                        geoQuery.removeAllListeners();
                        mRequest.setText("no bus found, try again");
                        return;
                    }
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
    private Marker mDriverMarker;
    private void getDriverLocation(){
        DatabaseReference driverLocationref = FirebaseDatabase.getInstance().getReference().child("driverAvailable").child(driverAvailableID).child("l");
        driverLocationref.addValueEventListener(new ValueEventListener(){

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if( dataSnapshot.exists() ){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double lat = 0;
                    double lng = 0;
                    if( map.get( 0 ) != null ){
                        lat = Double.parseDouble( map.get(0).toString() );
                    }
                    if( map.get( 1 ) != null ){
                        lng = Double.parseDouble( map.get(1).toString() );
                    }
                    LatLng driverLatlng = new LatLng(lat, lng );
                    if( mDriverMarker != null ){
                        mDriverMarker.remove();
                    }
                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatlng).title("Your bus is here"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    ///Close Drawer after press the back button.
    @Override
    public void onBackPressed() {
        if( drawerLayout.isDrawerOpen(GravityCompat.START) )
        {
            System.out.println("True");
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    ///Work some stuff with navigation menuItem
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        ///Close the drawer after select some item.
        drawerLayout.closeDrawer(GravityCompat.START);
        if( menuItem.getItemId() == R.id.userprofile )
        {
            final Intent intent = new Intent( this , user_profile.class );
            startActivity( intent );
        }
        else if( menuItem.getItemId() == R.id.fare )
        {
            final Intent intent = new Intent( this , fare_calculator.class );
            startActivity( intent );
        }
        else if( menuItem.getItemId() == R.id.routemap )
        {

        }
        else if( menuItem.getItemId() == R.id.businfo )
        {

        }
        else if( menuItem.getItemId() == R.id.reservation )
        {

        }
        else if( menuItem.getItemId() == R.id.feedback )
        {

        }
        else if( menuItem.getItemId() == R.id.notice )
        {

        }
        else if( menuItem.getItemId() == R.id.nav_logout )
        {
            isLogout = true;
            disconnect();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent( this , UserSelection.class );
            startActivity( intent );
            finish();
        }
        return true;
    }

    ///Map Services
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        buildGoogleApiClint();
        mMap.setMyLocationEnabled(true);
        if( ContextCompat.checkSelfPermission( this , Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION} , 1 );
        }
    }
    protected synchronized void buildGoogleApiClint() {
        mGoogleApiClint = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClint.connect();
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if( ContextCompat.checkSelfPermission( this , Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION} , 1 );
        }
        else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClint, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        lastlocation = location;
        progressDialog.dismiss();
        LatLng mylocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mylocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
        //mMap.addMarker(new MarkerOptions().position(sydney).title("You are here"));
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15.0f) );
        Geocoder geocoder = new Geocoder( getApplicationContext() , Locale.getDefault() );
        try {
            List<Address > listAddress = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if( listAddress != null && listAddress.size() > 0 )
            {
                showlocation = findViewById( R.id.current_location_name );
                String address = "";

                Log.i("Address" , listAddress.get(0).toString() );
                if( listAddress.get(0).getFeatureName() != null )
                {
                    address  += listAddress.get(0).getFeatureName();

                }
                showlocation.setText(address);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        double speed = location.getSpeed();
        speed *= 3.6;
        int intSpeed = (int)Math.floor( speed );
        showspeed = findViewById(R.id.current_speed);
        showspeed.setText( Integer.toString(intSpeed)+"Km/h" );

    }
    public void setupLayout() {
        ///Initialize navigation toolbar
        drawerLayout = (DrawerLayout) findViewById( R.id.drawer );
        toolbar = (Toolbar) findViewById( R.id.toolbar );
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        navigationView = (NavigationView) findViewById( R.id.navigationView );
        navigationView.setNavigationItemSelectedListener( this );
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);



        ///Show navigation indicator
        toggle = new ActionBarDrawerToggle(this, drawerLayout,toolbar,R.string.DrawOpen, R.string.DrawClose );
        drawerLayout.addDrawerListener(toggle);
        navigationView.bringToFront();
        toggle.setDrawerIndicatorEnabled(true);
        toggle.setDrawerSlideAnimationEnabled(true);
        toggle.syncState();

        //Show header data from profile database
        View headerView = navigationView.getHeaderView(0);
        headername = headerView.findViewById( R.id.person_name);
        headername.setText("Mahmudul Hasan Labib");
        headeremail = headerView.findViewById( R.id.person_email );
        headeremail.setText("mahmudul-xx-xxxx@diu.edu.bd");
    }
    public  void startProgress() {
        progressDialog = new ProgressDialog( Rider.this );
        progressDialog.show();
        progressDialog.setContentView( R.layout.progress_dialog);
    }
    public void disconnect(){
        try {
            String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if( userid != null ) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("riderAvailable");
                GeoFire geoFire = new GeoFire(ref);
                geoFire.removeLocation(userid);
            }
        }
        catch ( Exception e ) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
    }


}
