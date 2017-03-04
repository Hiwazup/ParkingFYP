package com.martin.parkingfyp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.martin.parkingfyp.model.CorkCarPark;
import com.martin.parkingfyp.model.CorkCarParkDetails;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin on 18/01/2017.

 */

public class CarParksAdapter extends RecyclerView.Adapter<CarParksAdapter.ViewHolder> {
    private ArrayList<CorkCarPark.Result.Records> mCarParks;
    private ArrayList<CorkCarParkDetails> mCorkCarParkDetails;
    private Context mContext;
    private DatabaseReference mDatabase = mDatabase = FirebaseDatabase.getInstance().getReference("carparks");

    public CarParksAdapter(Context context, ArrayList<CorkCarPark.Result.Records> carParks){
        mContext = context;
        mCarParks = carParks;
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //mCarParks.clear();
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    mCorkCarParkDetails.add(postSnapshot.getValue(CorkCarParkDetails.class));
                }
                notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Database",  "Failed to Read", databaseError.toException());
            }
        });
    }

    private Context getContext() {
        return mContext;
    }


    @Override
    public CarParksAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View carParkView = inflater.inflate(R.layout.list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(carParkView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CarParksAdapter.ViewHolder viewHolder, int position) {
        CorkCarPark.Result.Records carParks = mCarParks.get(position);
        TextView tit = viewHolder.title;
        tit.setText(carParks.getName());
        TextView spaces = viewHolder.spaces;
        TextView status = viewHolder.status;
        if(true){
            status.setText("Open");
            if(carParks.getFree_spaces() == 0){
                spaces.setText("No Spaces Available");
            } else {
                spaces.setText(Integer.toString(carParks.getFree_spaces()) + " Spaces");
            }
        } else {
            status.setText("Opens at 7am");
            spaces.setText("Closed");

        }
    }

    @Override
    public int getItemCount() {
        return mCarParks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView title;
        public TextView spaces;
        public TextView status;

        public ViewHolder(View itemView){
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            spaces = (TextView) itemView.findViewById(R.id.spaces);
            status = (TextView) itemView.findViewById(R.id.status);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), CarParkDetails.class);
            intent.putExtra("Name", title.getText());
            intent.putExtra("Spaces", spaces.getText());
            intent.putExtra("Status", status.getText());
            view.getContext().startActivity(intent);
        }
    }
}
