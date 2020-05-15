package com.example.diushuttle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
public class user_reg_complete extends AppCompatActivity {

    public  class User{
        public String firstName, lastName, email, password;
        public  User(){}
        User( String firstName, String lastName, String email , String password ){
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.password = password;
        }
    }
    private  EditText fname , lname , npass, cpass;
    private String firstName, lastName, email, password;
    private Button button;
    ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_reg_complete);

        button = findViewById(R.id.btn_post_reg);
        mAuth = FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if( user != null )
                {
                    progressDialog.dismiss();
                    Intent intent = new Intent( user_reg_complete.this , Rider.class );
                    startActivity( intent );
                    finish();
                    return;
                }
            }
        };
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startProgress();
                if( getdata() == false )
                {
                    progressDialog.dismiss();
                    showerror();
                    return;
                }
                else
                {
                    mAuth.createUserWithEmailAndPassword( email, password ).addOnCompleteListener(user_reg_complete.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if( !task.isSuccessful()) {
                                progressDialog.dismiss();
                                try {
                                   throw task.getException();
                                }
                                catch (FirebaseAuthWeakPasswordException weakPassword) {
                                    Toast.makeText(user_reg_complete.this, "Password is too small.", Toast.LENGTH_SHORT).show();
                                }
                                catch (FirebaseAuthUserCollisionException existEmail){
                                    Toast.makeText(user_reg_complete.this, "This email already exits.", Toast.LENGTH_SHORT).show();
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                            else
                            {
                                User insertUser = new User( firstName , lastName , email, password );
                                String user_id = mAuth.getCurrentUser().getUid();
                                DatabaseReference current_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Rider").child(user_id);
                                current_user_db.setValue(insertUser);
                            }
                        }
                    });
                    return;
                }
            }
        });

    }
    public boolean getdata()
    {
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        fname = (EditText) findViewById(R.id.inp_fname );
        lname = (EditText) findViewById(R.id.inp_lname );
        npass = (EditText) findViewById(R.id.new_pass );
        cpass = (EditText) findViewById(R.id.confirm_pass);
        String pass1 , pass2;
        pass1 = npass.getText().toString();
        pass2 = cpass.getText().toString();
        System.out.println( pass1 + " " + pass2 + " " + email );
        if( pass1.isEmpty() == true || pass1.equals( pass2 ) == false ){
            System.out.println("come");
            return false;
        }
        firstName = fname.getText().toString();
        lastName = lname.getText().toString();
        password = pass1;
        return  true;
    }
    public void showerror()
    {
        Toast.makeText(this, "Please insert a valid password" , Toast.LENGTH_LONG ).show();
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
        progressDialog = new ProgressDialog( user_reg_complete.this );
        progressDialog.show();
        progressDialog.setContentView( R.layout.progress_dialog);
    }
}
