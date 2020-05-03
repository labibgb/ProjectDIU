package com.example.diushuttle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class user_profile extends AppCompatActivity {

    private  static TextView updatePasswor;
    public  static FragmentManager fragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        updatePasswor = (TextView)  findViewById( R.id.change_password );
        updatePasswor.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNewPassword();
            }
        } );
    }
    public  void setNewPassword()
    {
        Intent intent = new Intent( this, UpdatePassword.class );
        startActivity( intent );
    }
}