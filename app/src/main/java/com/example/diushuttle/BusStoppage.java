package com.example.diushuttle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class BusStoppage extends AppCompatActivity {

    private String url = "http://159.203.79.216:8080/api/stoppage/GLOBAL/getAll";
    private ListView stoppage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_stoppage);

        stoppage = (ListView) findViewById(R.id.bus_stops );
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
                            ArrayAdapter arrayAdapter = new ArrayAdapter(BusStoppage.this, android.R.layout.simple_expandable_list_item_1,allstops){
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
        );
        requestQueue.add( jsonArrayRequest );
    }
}
