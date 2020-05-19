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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;

public class DriverMap extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, NavigationView.OnNavigationItemSelectedListener , com.google.android.gms.location.LocationListener {

    //Declare all the layout and View
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    Menu menu;
    TextView headername, headeremail , showspeed, showlocation;
    boolean flag = false;
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClint;
    Location lastlocation;
    LocationRequest mLocationRequest;
    LocationManager locationManager;
    LocationListener locationListener;
    ProgressDialog progressDialog;
    SupportMapFragment mapFragment;
    boolean isLogout = false;
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if( requestCode == 1 ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    mapFragment.getMapAsync(DriverMap.this);
                }
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.drivermap);
        if( ContextCompat.checkSelfPermission( this , Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION} , 1 );
        }
        else{
            mapFragment.getMapAsync(this);
        }
        setupLayout();
        startProgress();
        //fragmentManager = getSupportFragmentManager();

    }

    //Driver request list
    List< Marker > markerList = new ArrayList<Marker>();
    private void riderRequest(){
        if( isLogout ){
            return;
        }
        String driverId = FirebaseAuth.getInstance().getUid();
        DatabaseReference allDrivers = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverId).child("customerRiderId").child("Source");

        GeoFire geoFire = new GeoFire( allDrivers );
        GeoQuery geoQuery = geoFire.queryAtLocation( new GeoLocation( lastlocation.getLatitude(), lastlocation.getLongitude()), 5000 );
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                for( Marker marker : markerList ){
                    if( marker.getTag().equals(key)){
                        return;
                    }
                }
                LatLng driverLocation = new LatLng( location.latitude , location.longitude );
                int height = 75;
                int width = 75;
                Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.person_point);
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
                Marker mDriverMarker = mMap.addMarker( new MarkerOptions().position( driverLocation ).icon(smallMarkerIcon) );

                if( markerList.size() < 36 ) {
                    if( !isLogout ) {
                        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driverAcceptRequest");
                        GeoFire geoFire = new GeoFire(ref);
                        geoFire.setLocation(userid, new GeoLocation(lastlocation.getLatitude(), lastlocation.getLongitude()));
                    }
                    mDriverMarker.setTag(key);
                    markerList.add(mDriverMarker);
                }

            }

            @Override
            public void onKeyExited(String key) {
                for( Marker marker : markerList ){
                    if( marker.getTag().equals(key)){
                        marker.remove();
                        markerList.remove(marker);
                        return;
                    }
                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for (Marker marker : markerList) {
                    if (marker.getTag().equals(key)) {
                        marker.setPosition(new LatLng(location.latitude, location.longitude));
                    }
                }
            }
            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

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
        Bundle bundle = new Bundle();
        bundle.putString("goto", "Driver");
        ///Close the drawer after select some item.
        drawerLayout.closeDrawer(GravityCompat.START);
        if( menuItem.getItemId() == R.id.userprofile )
        {

            final Intent intent = new Intent( this , user_profile.class );
            intent.putExtras( bundle );
            startActivity( intent );
        }
        else if( menuItem.getItemId() == R.id.current_request )
        {

        }
        else if( menuItem.getItemId() == R.id.complete_request )
        {

        }
        else if( menuItem.getItemId() == R.id.businfo ) {

        }
        else if( menuItem.getItemId() == R.id.feedback ) {
            final Intent intent = new Intent( this , FeedBack.class );
            intent.putExtras( bundle );
            startActivity( intent );

        }
        else if( menuItem.getItemId() == R.id.notice ) {
            final Intent intent = new Intent( this , NoticeBoard.class );
            intent.putExtras( bundle );
            startActivity( intent );
        }
        else if( menuItem.getItemId() == R.id.driver_logout )
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if( ContextCompat.checkSelfPermission( this , Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( DriverMap.this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION} , 1 );
        }
        buildGoogleApiClint();
        mMap.setMyLocationEnabled(true);
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

            ActivityCompat.requestPermissions( DriverMap.this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION} , 1 );
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
        riderRequest();
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

        //Store Rider Last Location.
        //System.out.println("Marker list " + markerList.size());
        if( !isLogout && markerList.size() == 36 ){
            disconnect();
        }
        if( !isLogout && markerList.size() < 36 ) {
            String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driverAvailable");
            GeoFire geoFire = new GeoFire(ref);
            geoFire.setLocation(userid, new GeoLocation(lastlocation.getLatitude(), lastlocation.getLongitude()));
        }
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
        progressDialog = new ProgressDialog( DriverMap.this );
        progressDialog.show();
        progressDialog.setContentView( R.layout.progress_dialog);
    }

    public  void disconnect() {
            String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if( userid != null ) {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("driverAvailable");
                GeoFire geoFire = new GeoFire(ref);
                geoFire.removeLocation(userid);
            }
    }
    @Override
    protected void onStop() {
        super.onStop();
        //if( !isLogout )
           //disconnect();

    }
}
