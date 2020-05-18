package com.example.diushuttle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BusStoppage extends AppCompatActivity {

    private String url = "http://159.203.79.216:8080/api/stoppage/GLOBAL/getAll";
    public String token = null;
    private ListView stoppage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_stoppage);
        stoppage = (ListView) findViewById(R.id.bus_stops );
        getToken();


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
                        token = "Bearer"+dataMap.get("Token").toString();
                        getStoppage();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void getStoppage(){
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            ArrayList<String> allstops = new ArrayList<String>();
                            for( int i = 0 ; i < response.length(); i++ )
                            {
                                JSONObject jsonObject = response.getJSONObject(i);
                                String stops = jsonObject.getString("stoppageName");
                                allstops.add(stops);
                            }
                            ArrayAdapter arrayAdapter = new ArrayAdapter(BusStoppage.this, android.R.layout.simple_list_item_1,allstops){
                                @NonNull
                                @Override
                                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                                    View view =  super.getView(position, convertView, parent);
                                    TextView tv = (TextView) view.findViewById(android.R.id.text1);

                                    // Set the text color of TextView (ListView Item)
                                    tv.setTextColor(getResources().getColor(R.color.colorText));
                                    tv.setTextSize(25);
                                    tv.setGravity(Gravity.CENTER);

                                    // Generate ListView Item using TextView
                                    return view;
                                }
                            };
                            stoppage.setAdapter(arrayAdapter);
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
                params.put("Authorization", token );
                //  params.put("content-type", "application/json");
                return params;
            }
        };
        requestQueue.add( jsonArrayRequest );
    }
}
