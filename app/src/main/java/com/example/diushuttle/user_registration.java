package com.example.diushuttle;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class user_registration extends AppCompatActivity {

    public void mailSubmit(View view)
    {
        EditText editText = findViewById(R.id.reg_email);
        String email = editText.getText().toString();
        editText.setText("");
        boolean isOk = mailValidation( email );
        if ( isOk == true ){

            Intent intent = new Intent( this , user_reg_complete.class );
            startActivity( intent );
        }
        else{

            Toast.makeText(this, "Please insert a valid email" , Toast.LENGTH_LONG ).show();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);
    }
    public boolean mailValidation( String mail )
    {
        String valid = "@diu.edu.bd";
        int index = mail.indexOf( valid );
        return  index > 0;
    }
}
