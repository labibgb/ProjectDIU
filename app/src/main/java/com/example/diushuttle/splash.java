package com.example.diushuttle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class splash extends AppCompatActivity {
    private ImageView image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        image = (ImageView) findViewById(R.id.logo );
        Animation myanimation = AnimationUtils.loadAnimation(this, R.anim.mytranstion );
        image.startAnimation( myanimation );
        final Intent[] intent = new Intent[1];
        final boolean[] flag = {false};
        final String userid;
        if(FirebaseAuth.getInstance().getCurrentUser() != null ) {
            userid = FirebaseAuth.getInstance().getUid();
            DatabaseReference who = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(userid);
            who.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if( dataSnapshot.exists() ){
                        flag[0] = true;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            if( flag[0] == true ) {
                intent[0] = new Intent(this, DriverMap.class);
            }
            else {
                intent[0] = new Intent(this, Rider.class);
            }
        }
        else {
            intent[0] = new Intent(this, UserSelection.class);
        }
        Thread timer = new Thread() {
            public  void  run()
            {
                try {
                    sleep(3300);
                }
                catch ( InterruptedException e )
                {
                    e.printStackTrace();
                }
                finally {
                    startActivity(intent[0]);
                    finish();
                }
            }
        };
        timer.start();
    }
}
