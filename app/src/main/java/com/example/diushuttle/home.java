package com.example.diushuttle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

public class home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    //Declare all the layout and View
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    Menu menu;
    TextView headername, headeremail;
    boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ///Initialize navigation toolbar
        drawerLayout = (DrawerLayout) findViewById( R.id.drawer );
        toolbar = (Toolbar) findViewById( R.id.toolbar );
        setSupportActionBar(toolbar);
        navigationView = (NavigationView) findViewById( R.id.navigationView );
        navigationView.setNavigationItemSelectedListener( this );
        getSupportActionBar().setDisplayShowTitleEnabled(false);

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

        return true;
    }
}
