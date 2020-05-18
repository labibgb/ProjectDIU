package com.example.diushuttle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

public class fare_calculator extends AppCompatActivity {

    public class Stoppage{
        private int id;
        private String StoppageName;
        private double lat,lng;
        Stoppage(){};
        Stoppage( int id , String StoppageName, double lat , double lng ){
            this.id = id;
            this.StoppageName = StoppageName;
            this.lat = lat;
            this.lng = lng;
        }

        @NonNull
        @Override
        public String toString() {
            return this.StoppageName;
        }
        public double getLat(){
            return this.lat;
        }
        public double getLng(){
            return this.lng;
        }
    }
    private Spinner spinner1 , spinner2;
    private Button button;
    private TextView fareResult;
    private String url = "http://159.203.79.216:8080/api/stoppage/GLOBAL/getAll";
    private Location from = new Location("");
    private Location to = new Location("");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fare_calculator);
        spinner1 = (Spinner) findViewById(R.id.spiner1);
        spinner2 = (Spinner) findViewById(R.id.spiner2);
        button = (Button) findViewById(R.id.fare_submit);
        fareResult = (TextView) findViewById(R.id.fare_result);
        getToken();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double distance = from.distanceTo(to);
                distance = Math.ceil( distance/1000.00 );
                int taka = 5;
                distance--;
                distance *= 2.5;
                distance = (int) Math.ceil( distance );
                taka += distance;
                if( taka > 25 ){
                    taka = 25;
                }
                String res = Integer.toString( taka );
                fareResult.setText(res+" tk");
            }
        });
    }
    private String token = null;
    private void getToken(){
        String customerId = FirebaseAuth.getInstance().getUid();
        DatabaseReference findToken = FirebaseDatabase.getInstance().getReference().child("Token").child(customerId);
        findToken.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if( dataSnapshot.exists() ) {
                    HashMap<String, Object> dataMap = (HashMap<String, Object>) dataSnapshot.getValue();
                    if( dataMap.get("Token") != null ){
                        token = "Bearer "+dataMap.get("Token").toString();
                        addItemSpinner();
                        addListenerOnItemSelection();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void addItemSpinner(){


        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            ArrayList<Object> allstops = new ArrayList<Object>();
                            for( int i = 0 ; i < response.length(); i++ )
                            {
                                JSONObject jsonObject = response.getJSONObject(i);
                                Stoppage stoppage = new Stoppage(jsonObject.getInt("stoppageId"), jsonObject.getString("stoppageName"
                                ), jsonObject.getDouble("latitude"), jsonObject.getDouble("longitude") );
                                allstops.add(stoppage);
                            }
                            ArrayAdapter arrayAdapter = new ArrayAdapter(fare_calculator.this,R.layout.style_spinner,allstops);
                            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner1.setAdapter(arrayAdapter);
                            spinner2.setAdapter(arrayAdapter);
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
    public void addListenerOnItemSelection(){
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Stoppage stoppage = (Stoppage) parent.getSelectedItem();
                from.setLatitude(stoppage.getLat());
                from.setLongitude(stoppage.getLng());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Stoppage stoppage = (Stoppage) parent.getSelectedItem();
                to.setLatitude(stoppage.getLat());
                to.setLongitude(stoppage.getLng());

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
