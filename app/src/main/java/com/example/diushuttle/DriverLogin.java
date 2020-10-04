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
import com.auth0.android.jwt.Claim;
import com.auth0.android.jwt.JWT;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DriverLogin extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    EditText email , password;
    Button mLogin;
    boolean driverOk;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);
        driverOk = false;
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

                   // checkDriver(user.getUid());

//                    if( driverOk == true) {
//
//
//                    }
//                    else {
//                        Toast.makeText( DriverLogin.this, "You are not a driver." , Toast.LENGTH_SHORT ).show();
//                        DriverMap.disconnect();
//                        FirebaseAuth.getInstance().signOut();
//                        Intent intent = new Intent( DriverLogin.this , UserSelection.class );
//                        startActivity( intent );
//                        finish();
//                        return;
//                    }
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
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("username",Email);
                    jsonObject.put("password",Password );
                    loginData( jsonObject );
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
    public void loginData( JSONObject jsonObject ){
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
                       // String userId = mAuth.getCurrentUser().getUid();
                        //DatabaseReference Token = FirebaseDatabase.getInstance().getReference().child("Token").child(userId);
                        //token.setValue( response );
                        String token = null;
                        HashMap map = new HashMap();
                        try {
                            token = response.getString("jwt");
                            map.put("Token", token);
                            //Token.setValue(map);
                            JWT jwt = new JWT( token );
                            String email =  jwt.getClaim("sub").asString();
                            signup( email , token , jsonObject.getString("password"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Response: " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(DriverLogin.this, "Email or Password doesn't match.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        requestQueue.add(jsonObjectRequest);
    }
    public  class User{
        public String firstName, lastName, email, phone;
        public  User(){}
        User( String firstName, String lastName, String email , String phone ){
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phone = phone;
        }
    }
    public  void checkDriver(String driverId){
        DatabaseReference who = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverId);

        who.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if( dataSnapshot.exists() ) {
                    driverOk = true;
                    return;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void signup( String email , String token , String password ){

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        final String url = "http://mytestservice.live:8080/api/user/GLOBAL/"+email;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mAuth.createUserWithEmailAndPassword( email , password ).addOnCompleteListener(DriverLogin.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if( !task.isSuccessful()) {
                                    try {
                                        throw task.getException();
                                    }

                                    catch (FirebaseAuthUserCollisionException existEmail){
                                        mAuth.signInWithEmailAndPassword( email, password ).addOnCompleteListener(DriverLogin.this, new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                if( user != null ) {
                                                    String userId = user.getUid();
                                                    DatabaseReference Token = FirebaseDatabase.getInstance().getReference().child("Token").child(userId);
                                                    //token.setValue( response );
                                                    HashMap map = new HashMap();
                                                    map.put("Token", token);
                                                    Token.setValue(map);
                                                }
                                                return;
                                            }
                                        });
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                }
                                else {
                                    User insertUser = null;
                                    try {
                                        insertUser = new User( response.getString("firstName") , response.getString("lastName") , response.getString("email"), response.getString("mobileNo") );
                                        String user_id = mAuth.getCurrentUser().getUid();
                                        DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(user_id);
                                        current_user_db.setValue(insertUser);
                                        DatabaseReference Token = FirebaseDatabase.getInstance().getReference().child("Token").child(user_id);
                                        HashMap map = new HashMap();
                                        map.put("Token", token );
                                        Token.setValue(map);
                                        return;
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                        });
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println("some error");
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer "+token);
                //  params.put("content-type", "application/json");
                return params;
            }
        };
        requestQueue.add(jsonObjectRequest);
    }
}
