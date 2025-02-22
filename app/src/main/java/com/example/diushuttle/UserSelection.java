package com.example.diushuttle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class UserSelection extends AppCompatActivity {

    private Button rider , driver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_selection);

        rider = findViewById( R.id.rider );
        driver = findViewById( R.id.driver );

        rider.setOnClickListener(v -> openRider());

        driver.setOnClickListener(v -> openDriver());
    }
    public void openRider()
    {
        Intent intent = new Intent( this , MainActivity.class );
        startActivity( intent );
        finish();
    }
    public void  openDriver()
    {
        Intent intent = new Intent( this , DriverLogin.class );
        startActivity( intent );
        finish();
    }
}
