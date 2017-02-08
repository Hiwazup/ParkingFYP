package com.martin.parkingfyp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.martin.parkingfyp.model.CorkCarPark;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Martin on 18/01/2017.

 */

public class CarParksAdapter extends RecyclerView.Adapter<CarParksAdapter.ViewHolder> {
    private ArrayList<CorkCarPark.Result.Records> mCarParks;
    private Context mContext;

    public CarParksAdapter(Context context, ArrayList<CorkCarPark.Result.Records> carParks){
        mContext = context;
        mCarParks = carParks;
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
