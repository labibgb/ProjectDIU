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
import android.graphics.drawable.BitmapDrawable;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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

import java.util.ArrayList;
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
    private boolean afterBusFound = false;
    private LatLng pickupLocation;
    ProgressDialog progressDialog;
    boolean isLogout = false;
    private boolean isRequest = false;
    private int radius = 1;
    SupportMapFragment mapFragment;
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if( requestCode == 1 ) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    mapFragment.getMapAsync(Rider.this);
                }
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if( ContextCompat.checkSelfPermission( this , Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( Rider.this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION} , 1 );
        }
        else{
            mapFragment.getMapAsync(this);
        }

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
                    int height = 150;
                    int width = 150;
                    Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.pin_point1);
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                    BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
                    mMap.addMarker(new MarkerOptions().position(pickupLocation).icon( smallMarkerIcon ));
                    mRequest.setText("Please wait...");
                    radius = 1;
                    getClosestBus();
                }
            }
        });
    }

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
                            .child("customerRiderId");
                    GeoFire userGeo = new GeoFire(  driverref );
                    userGeo.setLocation(customerId, new GeoLocation(pickupLocation.latitude, pickupLocation.longitude));
                    //HashMap hashMap = new HashMap();
                    //hashMap.put("customerRiderId" , customerId );
                    //driverref.updateChildren( hashMap );
                    getDriverLocation();
                    mRequest.setText("Looking for bus");
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
        if(isRequest){
            mRequest.setText("Get the bus");
            isRequest = false;
            if( !isLogout ){
                removeRider();
            }
            return;
        }
        isRequest = true;
        DatabaseReference driverLocationref = FirebaseDatabase.getInstance().getReference().child("driverAcceptRequest").child(driverAvailableID).child("l");
        driverLocationref.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if( dataSnapshot.exists() ){
                    afterBusFound = true;
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
                    if( !isLogout ){
                        disconnect();
                    }

                    Location lo1 = new Location("");
                    Location lo2 = new Location("");
                    lo1.setLatitude(pickupLocation.latitude);
                    lo1.setLongitude(pickupLocation.latitude);

                    lo2.setLatitude(lat);
                    lo2.setLongitude(lng);
                    double distance = lo1.distanceTo(lo2);
                    if( distance < 100 ){
                        mRequest.setText("Bus's here");
                    }
                    else{
                        mRequest.setText("Bus found, please wait...");
                    }
                    int height = 75;
                    int width = 75;
                    Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.bus_icon1);
                    Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                    BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
                    mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatlng).title("Your bus is here").icon( smallMarkerIcon ));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    List< Marker > markerList = new ArrayList<Marker>();
    private void driverAroundMe(){
        DatabaseReference allDrivers = FirebaseDatabase.getInstance().getReference().child("driverAvailable");
        GeoFire geoFire = new GeoFire( allDrivers );
        GeoQuery geoQuery = geoFire.queryAtLocation( new GeoLocation( lastlocation.getLatitude(), lastlocation.getLongitude()), 5000 );
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if( afterBusFound ) return;
                for( Marker marker : markerList ){
                    if( marker.getTag().equals(key)){
                        return;
                    }
                }
                LatLng driverLocation = new LatLng( location.latitude , location.longitude );
                int height = 75;
                int width = 75;
                Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.bus_icon1);
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
                Marker mDriverMarker = mMap.addMarker( new MarkerOptions().position( driverLocation ).icon(smallMarkerIcon) );
                mDriverMarker.setTag(key);
                markerList.add(mDriverMarker);
            }

            @Override
            public void onKeyExited(String key) {
                if( afterBusFound ) return;
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
                if( afterBusFound ) return;
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
        else if( menuItem.getItemId() == R.id.stoppages )
        {
            final Intent intent = new Intent( this , BusStoppage.class );
            startActivity( intent );
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
            removeRider();
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

        if( ContextCompat.checkSelfPermission( this , Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( Rider.this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION} , 1 );
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

            ActivityCompat.requestPermissions( Rider.this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION} , 1 );
        }
        else{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClint , mLocationRequest , this );
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
        driverAroundMe();
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

        headeremail = headerView.findViewById( R.id.person_email );
        String customerId = FirebaseAuth.getInstance().getUid();
        DatabaseReference userInfo = FirebaseDatabase.getInstance().getReference().child("Users").child("Rider").child(customerId);

        userInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if( dataSnapshot.exists() ){
                    HashMap<String, Object> dataMap = (HashMap<String, Object>) dataSnapshot.getValue();
                    String firstName = "", lastName = "", uemail = "";
                    if( dataMap.get("firstName") != null ){
                        firstName = dataMap.get("firstName").toString();
                    }
                    if( dataMap.get("lastName") != null ){
                        lastName = dataMap.get("lastName").toString();
                    }
                    if( dataMap.get("email") != null ){
                        uemail = dataMap.get("email").toString();
                    }
                    headername.setText( firstName + " " + lastName );
                    headeremail.setText(uemail);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


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
    public void removeRider() {
        try {
            String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference driverref = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver")
                    .child(driverAvailableID)
                    .child("customerRiderId");
            GeoFire geoFire1 = new GeoFire(driverref);
            geoFire1.removeLocation(userid);
            driverref = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverAvailableID);
            driverref.setValue(true);
        }
        catch ( Exception e ){

        }

    }
    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }


}
