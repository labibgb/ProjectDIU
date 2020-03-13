package com.example.diushuttle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class user_reg_complete extends AppCompatActivity {

    public  void signup(View view )
    {

        //Get useer information to send it database.
        EditText firstName = findViewById(R.id.inp_fname );
        String fname = firstName.getText().toString();
        Toast.makeText(this, fname , Toast.LENGTH_SHORT).show();

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_reg_complete);
    }
}
