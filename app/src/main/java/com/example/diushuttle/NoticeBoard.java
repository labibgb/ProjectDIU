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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NoticeBoard extends AppCompatActivity {


    public class allNotice implements Comparable<allNotice> {
        private String date;
        private String data;
        allNotice( String x , String y ){
            this.date = x;
            this.data = y;
        }

        @NonNull
        @Override
        public String toString() {
            return "Date : "+getDate()+"\n"+"Notice : "+getData()+"\n";
        }

        public String getDate(){
            return this.date;
        }
        public String getData(){
            return this.data;
        }

        @Override
        public int compareTo(allNotice o) {
            return 1;
        }
    }
    class SortByName implements Comparator<allNotice> {
        @Override
        public int compare(allNotice a, allNotice b) {
            return a.getDate().compareTo(b.getDate());
        }
    }

    private String url = "http://159.203.79.216:8080/api/announcement/GLOBAL/all";
    public String token = null;
    private ListView notice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_board);
        notice = (ListView) findViewById(R.id.notice_board );
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
                        token = "Bearer "+dataMap.get("Token").toString();
                        getNotice();
                        getEmail();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    ArrayList<allNotice> all = new ArrayList<>();
    public void getNotice(){
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {

                            for( int i = 0 ; i < response.length(); i++ )
                            {
                                JSONObject jsonObject = response.getJSONObject(i);
                                String date = jsonObject.getString("date");

                                date = date.substring( 0 , 10 );
                                String announcement = jsonObject.getString("announcement");
                                all.add(new allNotice(  date , announcement ));

                            }
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
    public  void  getEmail(){
        Bundle bundle = getIntent().getExtras();
        String roll = bundle.getString("goto");
        String customerId = FirebaseAuth.getInstance().getUid();
        DatabaseReference userInfo = FirebaseDatabase.getInstance().getReference().child("Users").child(roll).child(customerId);
        userInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if( dataSnapshot.exists() ) {
                    HashMap<String, Object> dataMap = (HashMap<String, Object>) dataSnapshot.getValue();

                    if (dataMap.get("email") != null) {
                        String email = dataMap.get("email").toString();
                        String Url1 = "http://159.203.79.216:8080/api/user/GLOBAL/";
                        Url1 += email;
                        getUserId(Url1);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    public void getUserId( String Url){
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
                            String UrlId = "http://mytestservice.live:8080/api/user/notification/"+ID;
                            System.out.println(UrlId);
                            getFeedback(UrlId);
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
        requestQueue.add( jsonObjectRequest );
    }
    public void getFeedback( String urlId){
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                urlId,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {

                            for( int i = 0 ; i < response.length(); i++ )
                            {
                                JSONObject jsonObject = response.getJSONObject(i);
                                String date = jsonObject.getString("date");
                                date = date.substring( 0 , 10 );
                                String announcement = jsonObject.getString("notification");
                                all.add(new allNotice(  date , announcement ));
                            }

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
        Collections.sort(all, new SortByName());
        Collections.reverse(all);
        System.out.println(all);
        ArrayAdapter arrayAdapter = new ArrayAdapter(NoticeBoard.this, android.R.layout.simple_list_item_1,all){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view =  super.getView(position, convertView, parent);
                TextView tv = (TextView) view.findViewById(android.R.id.text1);

                // Set the text color of TextView (ListView Item)
                tv.setTextColor(getResources().getColor(R.color.colorText));
                tv.setTextSize(25);

                // Generate ListView Item using TextView
                return view;
            }
        };
        notice.setAdapter(arrayAdapter);
    }
}
