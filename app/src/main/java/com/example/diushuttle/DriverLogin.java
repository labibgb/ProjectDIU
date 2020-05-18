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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

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
                                        JSONObject jsonObject = new JSONObject();
                                        try {
                                            jsonObject.put("username",email);
                                            jsonObject.put("password",password );
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        loginData( jsonObject );
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
    public  void loginData( JSONObject jsonObject ){
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = "http://mytestservice.live:8080/api/login";

        System.out.println( jsonObject );
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String userId = mAuth.getCurrentUser().getUid();
                        DatabaseReference token = FirebaseDatabase.getInstance().getReference().child("Token").child(userId);
                        //token.setValue( response );
                        HashMap map = new HashMap();
                        try {
                            map.put("Token", response.getString("jwt"));
                            token.setValue(map);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Response: " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("some error");
                    }
                }
        );
        requestQueue.add(jsonObjectRequest);
    }
}
