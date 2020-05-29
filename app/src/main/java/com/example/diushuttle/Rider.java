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
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;

public class Rider extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, NavigationView.OnNavigationItemSelectedListener , com.google.android.gms.location.LocationListener, RoutingListener {


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
    private boolean afterBusFound = false;
    private LatLng pickupLocation;
    ProgressDialog progressDialog;
    boolean isLogout = false;
    private boolean isRequest = false;
    private int radius = 1;
    SupportMapFragment mapFragment;
    private Marker mPickupMarker;
    AutoCompleteTextView autoCompleteTextView;
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
        polylines = new ArrayList<>();
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if( ContextCompat.checkSelfPermission( this , Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( Rider.this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION} , 1 );
        }
        else{
            mapFragment.getMapAsync(this);
        }
        autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.destination);
        setupLayout();
        startProgress();
        getToken();
        mRequest = findViewById(R.id.send_request);
        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Store Rider Last Location.
                if( !isLogout) {
                    if( isRequest ){
                        removeMarker();
                        isRequest = false;
                        geoQuery.removeAllListeners();
                        if( driverFound ) {
                            driverLocationref.removeEventListener(driverLocationrefListener);
                        }
                        if( driverAvailableID != null ) {
                            System.out.println("Come here");
                            removeRider();
                        }
                        driverAvailableID = null;
                        driverFound = false;
                        if( mPickupMarker != null ){
                            mPickupMarker.remove();
                        }
                        if (mDriverMarker != null) {
                            mDriverMarker.remove();
                        }
                        afterBusFound = false;
                        mRequest.setText("Get the bus");
                    }
                    else {
                        isRequest = true;
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
                        mPickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).icon(smallMarkerIcon));
                        mRequest.setText("Please wait or Cancel request");
                        radius = 1;
                        getItemSelectListener();
                        getClosestBus();
                    }
                }
            }
        });
    }

    private boolean driverFound = false;
    private String driverAvailableID;
    GeoQuery geoQuery;
    private void getClosestBus(){
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("driverAvailable");
        GeoFire geoFire = new GeoFire(driverLocation);
        geoQuery = geoFire.queryAtLocation( new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius );
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if( !driverFound && isRequest ) {
                    driverFound = true;
                    driverAvailableID = key;
                    String customerId = FirebaseAuth.getInstance().getUid();
                    DatabaseReference driverref = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver")
                            .child(driverAvailableID);
                    DatabaseReference source = driverref.child("PickUp");
                    GeoFire userGeo = new GeoFire(  source );
                    userGeo.setLocation(customerId, new GeoLocation(pickupLocation.latitude, pickupLocation.longitude));
                    DatabaseReference desti = driverref.child("Dest");
                    LatLng destLatLng;
                    if( dest != null ){
                        System.out.println("come: " + dest );
                        GeoFire userDesti = new GeoFire( desti );
                        userDesti.setLocation(customerId, new GeoLocation(dest.getLat(), dest.getLng()) );
                        destLatLng = new LatLng( dest.getLat() , dest.getLng() );
                        getRouteToDest( destLatLng );
                    }
                    //HashMap hashMap = new HashMap();
                    //hashMap.put("customerRiderId" , customerId );
                    //driverref.updateChildren( hashMap );
                    getDriverLocation();
                    mRequest.setText("Looking for bus or Cancel request");
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
                    getClosestBus();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void getRouteToDest(LatLng destLatLng) {
        Routing routing = new Routing.Builder()
                .travelMode(Routing.TravelMode.DRIVING)
                .withListener(this)
                .waypoints(new LatLng(lastlocation.getLatitude(), lastlocation.getLongitude()), destLatLng)
                .build();
        routing.execute();
    }

    private Marker mDriverMarker;
    private DatabaseReference driverLocationref;
    private  ValueEventListener driverLocationrefListener;
    private void getDriverLocation(){
        driverLocationref = FirebaseDatabase.getInstance().getReference().child("driverAcceptRequest").child(driverAvailableID).child("l");
        driverLocationrefListener = driverLocationref.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if( dataSnapshot.exists() && isRequest ){
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
                        mRequest.setText("Bus found, please wait or Cancel request");
                    }
                    if( allDriverMarker != null){
                        allDriverMarker.remove();
                    }
                    markerList.clear();
                    allBus.removeAllListeners();
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
    GeoQuery allBus;
    Marker allDriverMarker;
    private void driverAroundMe(){
        DatabaseReference allDrivers = FirebaseDatabase.getInstance().getReference().child("driverAvailable");
        GeoFire geoFire = new GeoFire( allDrivers );
        allBus = geoFire.queryAtLocation( new GeoLocation( lastlocation.getLatitude(), lastlocation.getLongitude()), 5000 );
        allBus.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                for( Marker marker : markerList ){
                    if(  marker.getTag().equals(key)){
                        return;
                    }
                }
                LatLng driverLocation = new LatLng( location.latitude , location.longitude );
                int height = 75;
                int width = 75;
                Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.bus_icon1);
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
                BitmapDescriptor smallMarkerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker);
                allDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLocation).icon(smallMarkerIcon));
                allDriverMarker.setTag(key);
                markerList.add(allDriverMarker);
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

        ///Close the drawer after select some item.
        Bundle bundle = new Bundle();
        bundle.putString("goto", "Rider");
        drawerLayout.closeDrawer(GravityCompat.START);
        if( menuItem.getItemId() == R.id.userprofile )
        {

            final Intent intent = new Intent( this , user_profile.class );
            intent.putExtras(bundle);
            startActivity( intent );
        }
        else if( menuItem.getItemId() == R.id.fare )
        {
            final Intent intent = new Intent( this , fare_calculator.class );
            intent.putExtras(bundle);
            startActivity( intent );
        }
        else if( menuItem.getItemId() == R.id.routemap )
        {

        }
        else if( menuItem.getItemId() == R.id.stoppages )
        {
            final Intent intent = new Intent( this , BusStoppage.class );
            intent.putExtras(bundle);
            startActivity( intent );
        }
        else if( menuItem.getItemId() == R.id.reservation )
        {

        }
        else if( menuItem.getItemId() == R.id.feedback )
        {
            final Intent intent = new Intent( this , FeedBack.class );
            intent.putExtras(bundle);
            startActivity( intent );
        }
        else if( menuItem.getItemId() == R.id.notice )
        {
            final Intent intent = new Intent( this , NoticeBoard.class );
            intent.putExtras(bundle);
            startActivity( intent );
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
        if( !afterBusFound ) {
            driverAroundMe();
        }
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
        if( isLogout ){
            return;
        }
        String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference driverref = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver")
                .child(driverAvailableID);
        DatabaseReference source = driverref.child("PickUp");
        DatabaseReference end = driverref.child("Dest");
        GeoFire geoFire = new GeoFire(source);
        geoFire.removeLocation( userid );
        geoFire = new GeoFire( end );
        geoFire.removeLocation( userid );

    }
    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
    private String url = "http://159.203.79.216:8080/api/stoppage/GLOBAL/getAll";
    public String token = null;
    private void getToken(){
        String customerId = FirebaseAuth.getInstance().getUid();
        DatabaseReference findToken = FirebaseDatabase.getInstance().getReference().child("Token").child(customerId);
        findToken.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if( dataSnapshot.exists() ) {
                    HashMap<String, Object> dataMap = (HashMap<String, Object>) dataSnapshot.getValue();
                    if( dataMap.get("Token") != null ){
                        token = "Bearer "+dataMap.get("Token").toString();
                        getStoppage();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};
    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortRouterIndex ) {

        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;
            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);
            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingCancelled() {

    }

    public class Stoppage{
        private int id;
        private String StoppageName;
        private double lat,lng;
        Stoppage(){};
        Stoppage( int id , String StoppageName, double lat , double lng ){
            this.id = id;
            this.StoppageName = StoppageName;
            this.lat = lat;
            this.lng = lng;
        }

        @NonNull
        @Override
        public String toString() {
            return this.StoppageName;
        }
        public double getLat(){
            return this.lat;
        }
        public double getLng(){
            return this.lng;
        }
    }
    Stoppage dest;
    public void getStoppage(){
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            ArrayList<Object> allstops = new ArrayList<Object>();
                            for( int i = 0 ; i < response.length(); i++ )
                            {
                                JSONObject jsonObject = response.getJSONObject(i);
                                Stoppage stoppage = new Stoppage(jsonObject.getInt("stoppageId"), jsonObject.getString("stoppageName"
                                ), jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude") );
                                allstops.add(stoppage);
                            }
                            ArrayAdapter arrayAdapter = new ArrayAdapter(Rider.this, android.R.layout.simple_list_item_1,allstops){
                                @NonNull
                                @Override
                                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                                    View view =  super.getView(position, convertView, parent);
                                    TextView tv = (TextView) view.findViewById(android.R.id.text1);

                                    // Set the text color of TextView (ListView Item)
                                    tv.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                                    tv.setTextSize(25);
                                    tv.setGravity(Gravity.CENTER);

                                    // Generate ListView Item using TextView
                                    return view;
                                }
                            };
                            autoCompleteTextView.setAdapter(arrayAdapter);
                            autoCompleteTextView.setThreshold(1);

                        }
                        catch (Exception e ){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", token );
                //  params.put("content-type", "application/json");
                return params;
            }
        };
        requestQueue.add( jsonArrayRequest );
    }
    public void getItemSelectListener() {
        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
                dest = (Stoppage) parent.getAdapter().getItem( position );
            }
        });
    }
    public void removeMarker() {
        for( Polyline line : polylines ){
            line.remove();
        }
        polylines.clear();
    }
}
