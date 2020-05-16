package com.example.diushuttle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverLogin extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    EditText email , password;
    Button mLogin;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);

        mAuth = FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if( user != null )
                {
                    progressDialog.dismiss();
                    openHome();
                    return;
                }
            }
        };
        mLogin = findViewById( R.id.driver_login );
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = (EditText) findViewById( R.id.driver_email);
                password = (EditText) findViewById( R.id.driver_password );
                String Email = email.getText().toString();
                String Password = password.getText().toString();
                if( Email.isEmpty() == true || Password.isEmpty() == true )
                {
                    Toast.makeText( DriverLogin.this, "Please insert a valid email or password" , Toast.LENGTH_SHORT ).show();
                    return;
                }
                startProgress();
                mAuth.signInWithEmailAndPassword( Email , Password ).addOnCompleteListener(DriverLogin.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if( !task.isSuccessful()) {
                            progressDialog.dismiss();
                            mAuth.createUserWithEmailAndPassword( Email, Password ).addOnCompleteListener(DriverLogin.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if( !task.isSuccessful()) {
                                        Toast.makeText(DriverLogin.this, "Somethings went wrong.", Toast.LENGTH_SHORT).show();
                                    }
                                    else {

                                        String user_id = mAuth.getCurrentUser().getUid();
                                        DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(user_id);
                                        current_user_db.setValue(true);
                                    }
                                }
                            });
                            return;
                        }
                    }
                });
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
    public void openHome()
    {
        Intent intent = new Intent( this , DriverMap.class );
        startActivity( intent );
        finish();
    }
    public  void startProgress() {
        progressDialog = new ProgressDialog( DriverLogin.this );
        progressDialog.show();
        progressDialog.setContentView( R.layout.progress_dialog);
    }
}
