package com.example.diushuttle;

import androidx.annotation.NonNull;
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
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Locale;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;

public class Rider extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

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
    LocationManager locationManager;
    LocationListener locationListener;
    ProgressDialog progressDialog;
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
        //fragmentManager = getSupportFragmentManager();

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

        mMap.setMapType(MAP_TYPE_NORMAL);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                Log.i("loaction" , location.toString() );
                mMap.clear();
                progressDialog.dismiss();
                LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions().position(sydney).title("You are here"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 15.0f) );
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
                int intSpeed = (int)Math.ceil( speed );
                showspeed = findViewById(R.id.current_speed);
                showspeed.setText( Integer.toString(intSpeed)+"Km/h" );


            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if( ContextCompat.checkSelfPermission( this , Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION} , 1 );
        }
        else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    public void setupLayout()
    {
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
    public  void startProgress()
    {
        progressDialog = new ProgressDialog( Rider.this );
        progressDialog.show();
        progressDialog.setContentView( R.layout.progress_dialog);
    }
}
