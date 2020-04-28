package com.example.diushuttle;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class showSpeed extends Fragment {

    public showSpeed() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_show_speed, container, false);
        //TextView textView = (TextView) rootView.findViewById(R.id.show_speed_fragment);
        //textView.setText("This this text");
        return  rootView;
    }
}
