package com.example.diushuttle;

import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class UpdateProfile extends Fragment {

    private TextView updateProfile;
    public UpdateProfile() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_update_profile, container, false);
        EditText fname = (EditText) rootView.findViewById(R.id.fname );
        EditText lname = (EditText) rootView.findViewById( R.id.lname );

        updateProfile = rootView.findViewById( R.id.change_password );
        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_profile.fragmentManager.beginTransaction().replace( R.id.profile_container, new UpdatePasswordFragment(), null ).commit();
            }
        });
        return  rootView;
    }
}
