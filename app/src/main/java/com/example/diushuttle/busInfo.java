package com.example.diushuttle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.auth0.android.jwt.JWT;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class busInfo extends AppCompatActivity {

    public TextView busId, busNumber, oil, gass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_info);
        busId = findViewById(R.id.bus_id);
        busNumber = findViewById(R.id.bus_number);
        oil = findViewById(R.id.oil);
        gass = findViewById(R.id.gass);
        getToken();
    }
    public String token = null;
    private void getToken(){
        String customerId = FirebaseAuth.getInstance().getUid();
        DatabaseReference findToken = FirebaseDatabase.getInstance().getReference().child("Token").child(customerId);
        findToken.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if( dataSnapshot.exists() ) {
                    HashMap<String, Object> dataMap = (HashMap<String, Object>) dataSnapshot.getValue();
                    if( dataMap.get("Token") != null ){
                        //token = "Bearer "+dataMap.get("Token").toString();
                        token = dataMap.get("Token").toString();
                        sendData(token);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void sendData(String tkn ) {

        String bearer = "Bearer "+tkn;
        JWT userInfo = new JWT(tkn);
        String email = userInfo.getSubject();
        String url = "http://mytestservice.live:8080/api/user/GLOBAL/"+email;
        getUserId(url, bearer);

    }
    public void getUserId( String Url, String bearer){
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                Url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            int id = response.getInt("userId");
                            String ID = Integer.toString( id );
                            String UrlId = "http://mytestservice.live:8080/api/schedule/GLOBAL/all";
                            showInfo(UrlId, bearer, id );
                        }
                        catch (Exception e ){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", bearer );
                //  params.put("content-type", "application/json");
                return params;
            }
        };
        requestQueue.add( jsonObjectRequest );
    }
    public void showInfo(String url, String bearer, int Id) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            String id = null, numb = null, ol = null, gs = null;
                            for( int i = 0 ; i < response.length(); i++ )
                            {
                                JSONObject jsonObject = response.getJSONObject(i);
                                Boolean complete = jsonObject.getBoolean("isComplete");
                                if( !complete ){
                                    JSONObject bus = (JSONObject) jsonObject.get("bus");
                                    JSONObject driver = (JSONObject) jsonObject.get("driver");
                                    JSONObject u = (JSONObject) driver.get("user");
                                    int idd = u.getInt("userId");
                                    if( idd == Id) {
                                        id = Integer.toString(bus.getInt("busId"));
                                        numb = bus.getString("number");
                                        ol = Integer.toString((int) bus.getDouble("oilTankCapacity"));
                                        gs = Integer.toString((int) bus.getDouble("gasCylinderCapacity"));
                                        break;
                                    }
                                }
                            }
                            busId.setText("Bus id: "+id);
                            busNumber.setText(numb);
                            oil.setText("Oil Capacity: "+ol+"L");
                            gass.setText("Gass Capacity: "+gs+"L");

                        }
                        catch (Exception e ){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", bearer );
                //  params.put("content-type", "application/json");
                return params;
            }
        };
        requestQueue.add( jsonArrayRequest );
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
