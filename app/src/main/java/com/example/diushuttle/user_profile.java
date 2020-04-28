package com.example.diushuttle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.View;

public class user_profile extends AppCompatActivity {

    public  static FragmentManager fragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        UpdateProfile updateProfile = new UpdateProfile();
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add( R.id.profile_container, updateProfile , null)
                .commit();

    }
}
