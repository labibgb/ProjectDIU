package com.example.diushuttle;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class helperClass {
    public String url = "";
    public String token = null;
    public Context context;
    public ListView listView;
    public helperClass(String url, String token, Context context, ListView listView){
        this.token = token;
        this.url = url;
        this.context = context;
        this.listView = listView;
    }
    public void  getData(){
        ArrayList<String> route = new ArrayList<>();
        RequestQueue requestQueue = Volley.newRequestQueue(context);
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
                                int id = jsonObject.getInt("routeId");
                                JSONArray routeObj = jsonObject.getJSONArray("routeDetail");
                                String dt = "";
                                dt += "Route: " + id + "\n";
                                int f = 0;
                                for( int j = 0 ; j < routeObj.length(); j++ )
                                {
                                    JSONObject rt = routeObj.getJSONObject(j);
                                    String name = rt.getString("stoppageName");
                                    if(f == 1)
                                    {
                                        dt += ", ";
                                    }
                                    f = 1;
                                    dt += name;
                                }
                                route.add(dt);

                            }
                            ArrayAdapter arrayAdapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1,route){
                                @NonNull
                                @Override
                                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                                    View view =  super.getView(position, convertView, parent);
                                    TextView tv = (TextView) view.findViewById(android.R.id.text1);

                                    // Set the text color of TextView (ListView Item)
                                    tv.setTextColor(context.getResources().getColor(R.color.colorText));
                                    tv.setTextSize(25);

                                    // Generate ListView Item using TextView
                                    return view;
                                }
                            };
                            listView.setAdapter(arrayAdapter);
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
