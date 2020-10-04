package com.example.diushuttle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class routeMap extends AppCompatActivity {

    private ListView routeMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_map);
        routeMap = findViewById(R.id.route_list);
        getToken();
    }

    private void showRoute(String token) {
        helperClass data = new helperClass("http://mytestservice.live:8080/api/route/GLOBAL/all",
                token,
                this,
                routeMap
        );
        data.getData();

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
    private void getToken(){
        String customerId = FirebaseAuth.getInstance().getUid();
        DatabaseReference findToken = FirebaseDatabase.getInstance().getReference().child("Token").child(customerId);
        findToken.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if( dataSnapshot.exists() ) {
                    HashMap<String, Object> dataMap = (HashMap<String, Object>) dataSnapshot.getValue();
                    if( dataMap.get("Token") != null ){
                        String token = "Bearer "+dataMap.get("Token").toString();
                        showRoute(token);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
