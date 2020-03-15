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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    //Declare all the layout and View
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    Menu menu;
    TextView headername, headeremail , text0;

    boolean flag = false;
    /*public void getWeather( View view )
    {
        EditText location = findViewById(R.id.location);
        jsonSample sample = new jsonSample();
        String api = "https://samples.openweathermap.org/data/2.5/weather?q="+location.getText().toString()+"&appid=b6907d289e10d714a6e88b30761fae22";
        sample.execute(api);
    }*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Check Permission

        ///send api link to get data


        ///Initialize navigation toolbar
        drawerLayout = (DrawerLayout) findViewById( R.id.drawer );
        toolbar = (Toolbar) findViewById( R.id.toolbar );
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        navigationView = (NavigationView) findViewById( R.id.navigationView );
        navigationView.setNavigationItemSelectedListener( this );
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        ///Hide or visible navigation menu item. Initially only Login menu visible.
       /* menu = navigationView.getMenu();
        menu.findItem(R.id.nav_logout).setVisible(false);
        menu.findItem(R.id.userprofile).setVisible(false);
        menu.findItem(R.id.feedback).setVisible(false);
        menu.findItem(R.id.notice).setVisible(false);
        menu.findItem(R.id.businfo).setVisible(false);
        menu.findItem(R.id.reservation).setVisible(false);*/

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
        //text0 = findViewById(R.id.text0);
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

        if( menuItem.getItemId() == R.id.nav_login ){

            /*menu.findItem(R.id.nav_logout).setVisible(true);
            menu.findItem(R.id.userprofile).setVisible(true);
            menu.findItem(R.id.feedback).setVisible(true);
            menu.findItem(R.id.notice).setVisible(true);
            menu.findItem(R.id.businfo).setVisible(true);
            menu.findItem(R.id.reservation).setVisible(true);
            menu.findItem(R.id.nav_login).setVisible(false);*/
            final Intent intent = new Intent( this , MainActivity.class );
            startActivity( intent );

        }
        else if( menuItem.getItemId() == R.id.nav_logout )
        {
            /*menu.findItem(R.id.nav_logout).setVisible(false);
            menu.findItem(R.id.userprofile).setVisible(false);
            menu.findItem(R.id.feedback).setVisible(false);
            menu.findItem(R.id.notice).setVisible(false);
            menu.findItem(R.id.businfo).setVisible(false);
            menu.findItem(R.id.reservation).setVisible(false);*
            menu.findItem(R.id.nav_login).setVisible(true);*/
        }
        else if( menuItem.getItemId() == R.id.userprofile )
        {
            final Intent intent = new Intent( this , user_profile.class );
            startActivity( intent );
        }
        else if( menuItem.getItemId() == R.id.fare )
        {
            final Intent intent = new Intent( this , MapsActivity.class );
            startActivity( intent );
        }

        return true;
    }
    ///Get data from APi.
    /*
    public  class jsonSample extends AsyncTask< String , Void , String >{


        @Override
        protected String doInBackground(String... urls) {

            String results = "";
            URL url;
            HttpURLConnection urlConnection = null;
            try {

                url = new URL( urls[ 0 ] );
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader( in );
                int data = reader.read();
                while ( data != -1 )
                {
                    char current = (char) data;
                    results += current;
                    data = reader.read();
                }
                return  results;
            }catch( Exception e ) {
                e.printStackTrace();

                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try{
                JSONObject jsonObject = new JSONObject(s);
                String weather = jsonObject.getString("weather");
                JSONArray array = new JSONArray( weather );
                for( int i = 0 ; i < array.length() ; i++ )
                {
                    JSONObject getData = array.getJSONObject( i );
                    text0.setText(getData.getString("description").toUpperCase());
                }
            }catch (Exception e )
            {
                e.printStackTrace();
            }

        }
    }*/
}
