package com.example.diushuttle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class user_profile extends AppCompatActivity {

    public  class User{
        public String firstName, lastName, email ;
        public  User(){}
        User( String firstName, String lastName, String email  ){
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
        }
        public String getFirstName(){
            return this.firstName;
        }
        public String getLirstName(){
            return this.lastName;
        }
        public String getEmail(){
            return this.email;
        }
    }
    private  static TextView updatePasswor;
    public  static FragmentManager fragmentManager;
    TextView name , email, fixedEmail;
    EditText fname, lname, phone;
    private Button button;
    public String roll;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        showUserInfo();
        updatePasswor = (TextView)  findViewById( R.id.change_password );
        updatePasswor.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setNewPassword();
            }
        });

        button = findViewById(R.id.submit_update);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = getIntent().getExtras();
                roll = bundle.getString("goto");
                String customerId = FirebaseAuth.getInstance().getUid();
                DatabaseReference userInfo = FirebaseDatabase.getInstance().getReference().child("Users").child(roll).child(customerId);
                DatabaseReference frname = userInfo.child("firstName") , lsname = userInfo.child("lastName"), mobile = userInfo.child("phone");
                frname.setValue(fname.getText().toString());
                lsname.setValue(lname.getText().toString());
                mobile.setValue(phone.getText().toString());
                Toast.makeText( user_profile.this, "Update Successful." , Toast.LENGTH_SHORT ).show();
            }
        });
    }
    public  void setNewPassword()
    {
        Intent intent = new Intent( this, UpdatePassword.class );
        startActivity( intent );
    }
    public void showUserInfo(){
        Bundle bundle = getIntent().getExtras();
        roll = bundle.getString("goto");
        System.out.println(roll);
        name = (TextView)findViewById(R.id.person_name);
        email = (TextView)findViewById(R.id.person_email);
        fname = (EditText)findViewById(R.id.fname);
        lname = (EditText)findViewById(R.id.lname);
        fixedEmail = (TextView)findViewById(R.id.email);
        phone = (EditText) findViewById(R.id.edit_number);
        String customerId = FirebaseAuth.getInstance().getUid();
        DatabaseReference userInfo = FirebaseDatabase.getInstance().getReference().child("Users").child(roll).child(customerId);
        userInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if( dataSnapshot.exists() ){
                    System.out.println("Datasnap");
                    HashMap<String, Object> dataMap = (HashMap<String, Object>) dataSnapshot.getValue();
                    String firstName = "", lastName = "", uemail = "", uphone = "";
                    if( dataMap.get("firstName") != null ){
                        firstName = dataMap.get("firstName").toString();
                    }
                    if( dataMap.get("lastName") != null ){
                        lastName = dataMap.get("lastName").toString();
                    }
                    if( dataMap.get("email") != null ){
                        uemail = dataMap.get("email").toString();
                    }
                    if( dataMap.get("phone") != null ){
                        uphone = dataMap.get("phone").toString();
                    }
                    name.setText( firstName + " " + lastName );
                    fname.setText(firstName);
                    lname.setText(lastName);
                    email.setText(uemail);
                    phone.setText(uphone);
                    fixedEmail.setText(uemail);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Nullable
    @Override
    public Intent getSupportParentActivityIntent() {
        return backToParent();
    }

    @Nullable
    @Override
    public Intent getParentActivityIntent() {
        return backToParent();
    }
    private Intent backToParent(){
        Intent intent = null;
        Bundle bundle = getIntent().getExtras();
        String ss = bundle.getString("goto");
        if( ss.equals("Rider") ){
            intent = new  Intent( this , Rider.class );

        }
        else{
            intent = new  Intent( this , DriverMap.class );
        }
        intent.setFlags( intent.FLAG_ACTIVITY_CLEAR_TOP | intent.FLAG_ACTIVITY_SINGLE_TOP );
        return  intent;
    }
}