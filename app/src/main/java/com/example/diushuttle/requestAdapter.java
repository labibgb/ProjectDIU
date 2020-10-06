package com.example.diushuttle;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.geofire.GeoFire;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class requestAdapter extends RecyclerView.Adapter<requestAdapter.requestClass> {

    Context context;
    ArrayList<userInfoClass> user;
    public requestAdapter(Context context, ArrayList<userInfoClass> user){
        this.context = context;
        this.user = user;
    }
    @NonNull
    @Override
    public requestClass onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.request_layout, parent, false);
        requestClass rcsl = new requestClass(view);
        return rcsl;
    }

    @Override
    public void onBindViewHolder(@NonNull requestClass holder, int position) {
        final userInfoClass u = user.get(position);
        holder.name.setText(u.getFname()+" "+u.getElname());
        holder.email.setText(u.getMail());
        holder.complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Complete", Toast.LENGTH_SHORT).show();
                String driverId = FirebaseAuth.getInstance().getUid();
                DatabaseReference allDrivers = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverId).child("CompleteRide");
                allDrivers.child(u.getUserId()).setValue(new TempUser(u.getFname()+" "+u.getElname(), u.getMail()));
                DatabaseReference driverref = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver")
                        .child(driverId);
                DatabaseReference source = driverref.child("PickUp");
                //source.child(userid+"riderId").setValue(null);
                DatabaseReference end = driverref.child("Dest");
                GeoFire geoFire = new GeoFire(source);
                geoFire.removeLocation( u.getUserId() );
                geoFire = new GeoFire( end );
                geoFire.removeLocation( u.getUserId() );
            }
        });
    }

    @Override
    public int getItemCount() {
        return user.size();
    }

    public class requestClass extends RecyclerView.ViewHolder{
        TextView name, email;
        Button complete;
        public requestClass(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.rider_name);
            email = itemView.findViewById(R.id.rider_email);
            complete = itemView.findViewById(R.id.ride_complete);

        }
    }
    public class TempUser{
        public  String name, email;
        public TempUser(String name, String email){
            this.name = name;
            this.email = email;
        }
    }
}
