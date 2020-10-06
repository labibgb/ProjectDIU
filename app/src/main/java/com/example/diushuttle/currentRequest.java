package com.example.diushuttle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;



public class currentRequest extends AppCompatActivity {

    ArrayList<userInfoClass> userInfoClasses = new ArrayList<>();
    RecyclerView req;
    RecyclerView.Adapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_request);
        req = findViewById(R.id.userRecycle);
        getRunningRequest();
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
    private void getRunningRequest() {
        String driverId = FirebaseAuth.getInstance().getUid();
        DatabaseReference allDrivers = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverId).child("PickUp");
        allDrivers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for( DataSnapshot ds : dataSnapshot.getChildren()){
                    String id = ds.getKey();

                    if( id.equals("dummy"))
                        continue;
                    DatabaseReference userInfo = FirebaseDatabase.getInstance().getReference().child("Users").child("Rider").child(id);
                    userInfo.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot datas) {
                            if (datas.exists()) {
                                HashMap<String, Object> dataMap = (HashMap<String, Object>) datas.getValue();
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
                                userInfoClass ucs = new userInfoClass(firstName, lastName, uemail, uphone);
                                System.out.println("test -->" +ucs.getElname());
                                ucs.setUserId(id);
                                userInfoClasses.add(ucs);
                                req.setHasFixedSize(true);
                                req.setLayoutManager( new GridLayoutManager( currentRequest.this, 1 ) );
                                adapter = new requestAdapter(currentRequest.this,userInfoClasses);
                                req.setAdapter(adapter);
                                ViewCompat.setNestedScrollingEnabled(req, false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showRequest() {
        req.setHasFixedSize(true);
        req.setLayoutManager( new GridLayoutManager( this, 1 ) );
        adapter = new requestAdapter(this,userInfoClasses);
        req.setAdapter(adapter);
        ViewCompat.setNestedScrollingEnabled(req, false);
    }
}
