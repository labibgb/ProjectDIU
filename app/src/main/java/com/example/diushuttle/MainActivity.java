package com.example.diushuttle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import  android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public void loginFunction( View view )
    {
        EditText email = findViewById( R.id.email);
        EditText password = findViewById( R.id.password );
        Log.i( "email" , email.getText().toString() );
        Log.i( "Password" , password.getText().toString() );
        Toast.makeText( this , "Hey " + email.getText().toString(),  Toast.LENGTH_LONG).show();
    }
    public void signupFunction( View view )
    {
        Intent intent = new Intent( this , user_registration.class );
        startActivity( intent );
        Toast.makeText(this, "Register Click" , Toast.LENGTH_LONG ).show();
    }

    public void forgetpassFunction( View view )
    {
        Toast.makeText(this, "Forget Password" , Toast.LENGTH_LONG ).show();
    }
    public void openHome()
    {
        Intent intent = new Intent( this , home.class );
        startActivity( intent );
        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
