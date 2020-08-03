package com.example.diushuttle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import  android.util.Log;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {



    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    Button login;
    ProgressDialog progressDialog;

    public void signupFunction( View view )
    {
        Intent intent = new Intent( this , user_registration.class );
        startActivity( intent );
    }

    public void forgetpassFunction( View view )
    {
        Toast.makeText(this, "Forget Password" , Toast.LENGTH_LONG ).show();
    }
    public void openHome()
    {
        Intent intent = new Intent( this , Rider.class );
        startActivity( intent );
        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        login = findViewById( R.id.btn_login );
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProgress();
                EditText email = (EditText) findViewById( R.id.email);
                EditText password = (EditText) findViewById( R.id.password );
                String Email = email.getText().toString();
                String Password = password.getText().toString();
                if( Email.isEmpty() == true || Password.isEmpty() == true )
                {

                    Toast.makeText( MainActivity.this, "Please insert a valid email or password" , Toast.LENGTH_SHORT ).show();
                    return;
                }
                mAuth.signInWithEmailAndPassword( Email , Password ).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if( !task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Email or Password doesn't match.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("username",Email);
                                jsonObject.put("password",Password );
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            sendData( jsonObject );
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
    public  void startProgress() {
        progressDialog = new ProgressDialog( MainActivity.this );
        progressDialog.show();
        progressDialog.setContentView( R.layout.progress_dialog);
    }
    public  void sendData( JSONObject jsonObject ){
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
                        System.out.println(error.getMessage());
                    }
                }
        );
        requestQueue.add(jsonObjectRequest);
    }
}
